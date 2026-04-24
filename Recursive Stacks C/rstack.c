#include "rstack.h"
#include <assert.h>
#include <errno.h>
#include <malloc.h>
#include <stddef.h>
#include <stdlib.h>
#include <string.h>
#include <stddef.h>
#include <inttypes.h>
#include <stdbool.h>
#include <stdio.h>
#include <ctype.h>

typedef struct node {
    struct node* next;
    rstack_t* val_st;
    bool is_stack;
    uint64_t val_num;

} node;


// GREY - needed for first DFS
// WHITE - for nodes that are ONLY reachable from a loop
// BLACK - for nodes that should be 'rescued' from deleting
typedef enum { BLACK, GREY, WHITE } color_t;

struct rstack {
    struct node* top;
    uint64_t ref_count;
    color_t color;     // For rstack_delete procedure
};


rstack_t* rstack_new() {
    rstack_t *stack = malloc(sizeof(rstack_t));

    if (stack == NULL) {
        errno = ENOMEM;
        return NULL;
    }

    stack -> ref_count = 1;
    stack -> top = NULL;
    stack -> color = BLACK;
    return stack;
}

// Forward declaration:
static bool write_dfs(rstack_t *rs, FILE *file);

static bool write_nodes_bottom_up(node* curr, FILE *file) {
    if (curr == NULL) {
        return true;
    }

    // We stop if there is a cycle
    if (!write_nodes_bottom_up(curr -> next, file)) {
        return false; 
    }

    if (curr -> is_stack) {
        // A cycle
        if (curr -> val_st -> color == GREY) {
            return false; 
        }
        
        // Need to stop printing because there is a cycle incoming
        if (!write_dfs(curr -> val_st, file)) {
            return false;
        }
    } 
    else {
        fprintf(file, "%" PRIu64 "\n", curr->val_num); 
    }
    
    return true; 
}

// Checks for incoming loops
static bool write_dfs(rstack_t *rs, FILE *file) {
    if (rs == NULL) {
        return true; 
    }
    if (rs -> color == GREY) {
        return false; 
    }
    
    rs -> color = GREY; 
    bool is_finite = write_nodes_bottom_up(rs -> top, file);
    rs -> color = BLACK; 

    return is_finite; 
}

// First DFS - 'deletes' inside edges
static void mark_grey(rstack_t *rs) {
    if (rs == NULL || rs -> color == GREY) {
        return;
    }

    rs -> color = GREY;

    node* curr = rs -> top;
    while (curr != NULL) {
        if (curr -> is_stack) {
            curr -> val_st -> ref_count--;
            mark_grey(curr -> val_st);    
        }
        curr = curr -> next;
    }
}

// Recreate inside edges which mark_grey temporarily deleted 
static void rescue_black(rstack_t *rs) {
    if (rs == NULL || rs->color == BLACK) return;

    rs->color = BLACK;
    
    node* curr = rs->top;
    while (curr != NULL) {
        if (curr->is_stack) {
            curr->val_st->ref_count++;
            rescue_black(curr->val_st);
        }
        curr = curr->next;
    }
}

// Deletes nodes with none 'external' edges
static void collect_white (rstack_t *rs, rstack_t **free_list) {
    if (rs == NULL || rs -> color != WHITE) {
        return;
    }
    
    // Marking as visited
    rs -> color = BLACK;
    node *curr = rs -> top;
    
    // Adding rs to free_list so it will be deleted at the end
    rs -> top = (node *)*free_list;
    *free_list = rs;

    // Deleting all nodes
    while (curr != NULL) {
        node *nxt = curr -> next;
        if (curr -> is_stack) {
            collect_white(curr->val_st, free_list);
        }
        free(curr);
        curr = nxt;
    }
}



rstack_t* rstack_read(char const *path) {
    if (path == NULL) {
        errno = EINVAL;
        return NULL;
    }

    FILE *file = fopen(path, "r");
    if (file == NULL) {
        return NULL;
    }

    rstack_t* st = rstack_new();
    if (st == NULL) { 
        fclose(file); 
        return NULL; 
    }

    char word[128];
    while (fscanf(file, "%127s", word) == 1) {
        // If a number is negative
        if (word[0] == '-') {
            goto read_err;
        }

        char *endptr;
        errno = 0;
        uint64_t val = strtoull(word, &endptr, 10);

        // If a number is too large or is not a decimal number
        if (errno == ERANGE || *endptr != '\0') {
            goto read_err;
        }

        if (rstack_push_value(st, (uint64_t)val) != 0) {
            goto read_err;
        }
    }

    // Checks if there's anything at the end of file
    int c;
    while ((c = fgetc(file)) != EOF) {
        if (!isspace(c)) {
            goto read_err;
        }
    }

    fclose(file);
    return st;

    read_err:
        rstack_delete(st);
        fclose(file);
        return NULL;
}


// Find and rescues all of the black nodes
void scan(rstack_t *rs) {
    if (rs == NULL) {
        errno = EINVAL; 
        return;
    }
    if (rs -> color != GREY) {
        return;
    }
    
    // If it has some external edge
    if (rs -> ref_count > 0) {
        rescue_black(rs);
    }
    else 
    {
        rs -> color = WHITE; // Candidate for deletion
        for (node *curr = rs -> top; curr != NULL; curr = curr -> next) {
            if (curr -> is_stack) { 
                scan(curr -> val_st);
            }
        }
    }
} 


// Deletes rstack with ref_count equals to zero
static void release(rstack_t *rs) {
    if (rs == NULL) {
        return;
    }

    rs -> color = BLACK;
    node *curr = rs -> top;
    while (curr != NULL) {
        node *nxt = curr -> next;
        if (curr -> is_stack) {
            rstack_delete(curr -> val_st);
        }
        free(curr);
        curr = nxt;
    }
    free(rs);
}


void rstack_delete(rstack_t *rs) {
    if (rs == NULL) { 
        errno = EINVAL; 
        return; 
    }
    rs -> ref_count--;
    
    if (rs -> ref_count == 0) {
        release(rs); 
    } 
    else {
        mark_grey(rs);
        scan(rs);
        
        // We put all white nodes on single list
        rstack_t *free_list = NULL;
        collect_white(rs, &free_list);  
        
        // Simply free the whole list
        while (free_list != NULL) {
            rstack_t *nxt = (rstack_t *)free_list -> top;
            free(free_list);
            free_list = nxt;
        }
    }
}


int rstack_push_value(rstack_t *rs, uint64_t value) {
    if (rs == NULL) {
        errno = EINVAL;
        return -1;   
    }

    node* dummy = malloc(sizeof(node));
    if (dummy == NULL) {
        errno = ENOMEM;
        return -1;   
    }

    dummy -> val_num = value;
    dummy -> next = rs -> top;
    dummy -> is_stack = false;

    rs -> top = dummy; 
    return 0;
}

int rstack_push_rstack(rstack_t *rs1, rstack_t *rs2) {
    if (rs1 == NULL || rs2 == NULL) {
        errno = EINVAL;
        return -1;
    }

    node* dummy = malloc(sizeof(node));
    if (dummy == NULL) {
        errno = ENOMEM;
        return -1;   
    }
    
    dummy -> val_st = rs2;
    dummy -> next = rs1 -> top;
    dummy -> is_stack = true;

    rs1 -> top = dummy; 
    rs2 -> ref_count++;

    return 0;
}

void rstack_pop(rstack_t *rs) {
    if (rs == NULL || rs -> top == NULL) {
        errno = EINVAL;
        return;
    }

    node* dummy = rs -> top;
    rs -> top = rs -> top -> next;
    if (dummy -> is_stack) {
        rstack_delete(dummy -> val_st);
    }
    
    // If rstack_delete did not free it
    if (dummy != NULL) {
        free(dummy);
    }
}


bool rstack_empty(rstack_t *rs) {
    if (rs == NULL) {
        errno = EINVAL; 
        return true;
    }
    if (rs -> top == NULL) {
        return true;
    }
    
    if (rs -> color == GREY) {
        return true;
    }
    
    bool found = false;

    rs -> color = GREY;
    node* curr = rs -> top;
    while (curr != NULL) {
        // Stop if number is found
        if (!(curr -> is_stack)) {
            found = true;
            break;
        }
        found = !rstack_empty(curr -> val_st);

        if (found) {
            break;
        }

        curr = curr -> next;
    }

    // Resets color 
    rs -> color = BLACK;

    return !found;
}


result_t rstack_front(rstack_t *rs) {
    result_t first_found = {.flag = false, .value = 0};

    if (rs == NULL) {
        errno = EINVAL; 
        return first_found;
    }
    if (rs -> top == NULL) {
        return first_found;
    }
    
    if (rs -> color == GREY) {
        return first_found;
    }
    
    rs -> color = GREY;
    node* curr = rs -> top;
    while (curr != NULL) {
        if (!(curr -> is_stack)) {
            first_found.flag = true;
            first_found.value = curr -> val_num;
            break;
        }
        first_found = rstack_front(curr -> val_st);

        if (first_found.flag) {
            break;
        }

        curr = curr -> next;
    }

    // Resets color
    rs -> color = BLACK;

    return first_found;
}

int rstack_write(char const *path, rstack_t *rs) {
    if (path == NULL || rs == NULL) {
        errno = EINVAL;
        return -1;
    }

    FILE *file = fopen(path, "w");
    if (file == NULL) {
        return -1;
    }

    write_dfs(rs, file); 
    
    fclose(file);
    return 0;
}
