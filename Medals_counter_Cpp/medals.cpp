#include <cstddef>
#include <functional>
#include <iostream>
#include <regex>
#include <string>
#include <string_view>
#include <tuple>
#include <unordered_map>
#include <vector>

namespace {
    // Year, event, medal. An empty string means the criterion is absent.
    using medal_key = std::tuple<std::string, std::string, std::string>;

    enum class word_type { question, medal, year, event, invalid };

    word_type classify(const std::string& word) {
        static const std::regex medal_regex("[123]");
        // Year from 1800 to 59999, without leading zeros.
        static const std::regex year_regex("1[89][0-9]{2}|[2-9][0-9]{3}|[1-5][0-9]{4}");
        static const std::regex event_regex("[A-Za-z]+");

        if (word == "?") {
            return word_type::question;
        }
        if (std::regex_match(word, medal_regex)) {
            return word_type::medal;
        }
        if (std::regex_match(word, year_regex)) {
            return word_type::year;
        }
        if (std::regex_match(word, event_regex)) {
            return word_type::event;
        }
        return word_type::invalid;
    }

    // Splits a line into words; only spaces and tabs count as separators.
    std::vector<std::string> split_into_words(const std::string& line) {
        constexpr std::string_view separators = " \t";
        std::vector<std::string> words;
        std::string::size_type start = line.find_first_not_of(separators);
        while (start != std::string::npos) {
            const auto end = line.find_first_of(separators, start);
            words.push_back(line.substr(start, end - start));
            start = line.find_first_not_of(separators, end);
        }
        return words;
    }

    // Validates a line (with or without a question mark) and fills in key and is_query.
    // A missing criterion is left as an empty string.
    bool parse_line(const std::string& line, medal_key& key, bool& is_query) {
        key = medal_key{};
        auto& [year, event, medal] = key;
        const std::vector<std::string> words = split_into_words(line);
        if (words.empty()) {
            return false;
        }

        is_query = classify(words.front()) == word_type::question;
        bool previous_was_event = false;

        for (std::size_t i = is_query ? 1 : 0; i < words.size(); ++i) {
            const word_type type = classify(words[i]);
            switch (type) {
                case word_type::medal:
                    if (!medal.empty()) {
                        return false;
                    }
                    medal = words[i];
                    break;
                case word_type::year:
                    if (!year.empty()) {
                        return false;
                    }
                    year = words[i];
                    break;
                case word_type::event:
                    if (previous_was_event) {
                        event += ' ';
                        event += words[i];
                    } else if (!event.empty()) {
                        return false;  // Event name split by another word.
                    } else {
                        event = words[i];
                    }
                    break;
                case word_type::question:
                case word_type::invalid:
                    return false;
            }
            previous_was_event = type == word_type::event;
        }

        const bool has_all_criteria =
            !medal.empty() && !year.empty() && !event.empty();
        return is_query || has_all_criteria;
    }

    // Mixes the hash of a single string into the result.
    void hash_combine(std::size_t& seed, const std::string& text) {
        // Constant taken from boost::hash_combine.
        constexpr std::size_t golden_ratio = 0x9e3779b9;
        const std::size_t text_hash = std::hash<std::string>{}(text);
        seed ^= text_hash + golden_ratio + (seed << 6) + (seed >> 2);
    }

    struct tuple_hash {
        std::size_t operator()(const medal_key& key) const {
            std::size_t seed = 0;
            hash_combine(seed, std::get<0>(key));
            hash_combine(seed, std::get<1>(key));
            hash_combine(seed, std::get<2>(key));
            return seed;
        }
    };

    using medal_counter = std::unordered_map<medal_key, std::size_t, tuple_hash>;

    // Records the medal under every combination of criteria; a missing criterion is an empty string.
    void add_medal(const medal_key& key, medal_counter& medal_counts) {
        const auto& [year, event, medal] = key;

        ++medal_counts[medal_key{"", "", ""}];

        ++medal_counts[medal_key{year, "", ""}];
        ++medal_counts[medal_key{"", event, ""}];
        ++medal_counts[medal_key{"", "", medal}];

        ++medal_counts[medal_key{year, event, ""}];
        ++medal_counts[medal_key{"", event, medal}];
        ++medal_counts[medal_key{year, "", medal}];

        ++medal_counts[medal_key{year, event, medal}];
    }

    std::size_t count_medals(const medal_key& query, const medal_counter& medal_counts) {
        const auto found = medal_counts.find(query);
        return found == medal_counts.end() ? 0 : found->second;
    }

    void process_input() {
        medal_counter medal_counts;
        medal_key key;
        bool is_query = false;
        std::string line;
        std::size_t line_number = 0;

        while (std::getline(std::cin, line)) {
            ++line_number;

            if (!parse_line(line, key, is_query)) {
                std::cerr << "ERROR " << line_number << '\n';
                continue;
            }

            if (is_query) {
                std::cout << count_medals(key, medal_counts) << '\n';
            } else {
                add_medal(key, medal_counts);
            }
        }
    }
}

int main() {
    process_input();
}