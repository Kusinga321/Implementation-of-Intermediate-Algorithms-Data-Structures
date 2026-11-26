#ifndef TRAINS_H
#define TRAINS_H

#include <array>
#include <compare>
#include <cstddef>
#include <initializer_list>
#include <optional>
#include <ostream>
#include <queue>
#include <ranges>
#include <string>
#include <string_view>
#include <utility>
#include <vector>
using namespace std;

namespace trains_details {
    enum class WagonType {
        FIRST_CLASS = 0,
        SECOND_CLASS = 1,
        SLEEPER = 2,
        DINING = 3,
    };

    inline constexpr std::array<size_t, 4> STANDARD_NUM_OF_SEATS{54, 80, 30, 32};
    inline constexpr std::array<std::string_view, 4> WAGON_TYPE_NAMES{"1 klasa", "2 klasa", "sypialny", "restauracyjny"};

    class WagonSeries {
    private:
        WagonType wagonType;
        size_t numOfSeats;

    public:
        constexpr explicit WagonSeries(const WagonType wType, const size_t numSeats) : wagonType(wType), numOfSeats(numSeats) {}

        constexpr WagonSeries(const WagonType wType) : wagonType(wType), numOfSeats(STANDARD_NUM_OF_SEATS[static_cast<size_t>(wType)]) {}

        // kopiowanie
        constexpr WagonSeries(const WagonSeries&) = default;
        // przenoszenie
        constexpr WagonSeries(WagonSeries&&) = default;

        // przypisanie kopiujące
        constexpr WagonSeries& operator=(const WagonSeries&) = default;
        // przypisanie przenoszące
        constexpr WagonSeries& operator=(WagonSeries&&) = default;

        constexpr bool operator==(const WagonSeries& w) const {
            return w.wagonType == this->wagonType && w.numOfSeats == this->numOfSeats;
        }

        // overloading cout operator
        friend std::ostream& operator<<(std::ostream& os, const WagonSeries& w) {
            os << "[" << WAGON_TYPE_NAMES[static_cast<size_t>(w.wagonType)] << ", miejsca: " << w.numOfSeats << "]";
            return os;
        }

        // Getters and Setters
        constexpr WagonType type() const {
            return wagonType;
        }

        constexpr size_t seats() const {
            return numOfSeats;
        }
    };

    inline constexpr WagonSeries STANDARD_FIRST_CLASS{WagonType::FIRST_CLASS};
    inline constexpr WagonSeries STANDARD_SECOND_CLASS{WagonType::SECOND_CLASS};
    inline constexpr WagonSeries STANDARD_SLEEPER{WagonType::SLEEPER};
    inline constexpr WagonSeries STANDARD_DINING{WagonType::DINING};

    class Wagon {
    public:
        using unique_id_t = size_t;

        explicit Wagon(WagonSeries wagonSeries) : wagonSeries(wagonSeries) {
            this->number_ = ++numCreated;
            in_service_++;
        }

        Wagon() {}

        Wagon(const Wagon& other) {
            if (other.number_ != 0) {
                wagonSeries = other.wagonSeries;
                number_ = ++numCreated;
                in_service_++;
            }
        }

        Wagon(Wagon&& other) noexcept : wagonSeries(std::move(other.wagonSeries)), number_(other.number_) {
            other.number_ = 0;
            // liczba in service sie nie zmienia.
        }

        Wagon& operator=(const Wagon& other) {
            wagonSeries = other.wagonSeries;
            if (not other.number_) {
                if (number_) {
                    in_service_--;
                }
                number_ = 0;
            } else if (not number_) {
                number_ = ++numCreated;
                in_service_++;
            }
            return *this;
        }

        Wagon& operator=(Wagon&& other) noexcept {
            if (this != &other) {
                if (number_) in_service_--;
                wagonSeries = std::move(other.wagonSeries);
                number_ = std::exchange(other.number_, 0);
            }
            return *this;
        }

        unique_id_t number() const {
            return number_;
        }

        WagonSeries series() const {
            return wagonSeries.value();
        }

        operator WagonSeries() const { return wagonSeries.value(); }
        explicit operator bool() const { return number_; }

        auto operator<=>(const Wagon& other) const { return number_ <=> other.number_; }
        bool operator==(const Wagon& other) const { return number_ == other.number_; }

        static size_t in_service() {
            return in_service_;
        }

        static size_t ever_built() {
            return numCreated;
        }

        friend ostream& operator<<(ostream& os, const Wagon& wagon) {
            os << "Wagon ";
            if (not wagon.number_) {
                os << "pusty";
            } else {
                os << "nr " << wagon.number() << " ";
                os << wagon.series();
            }

            return os;
        }

        size_t seats() const {
            return wagonSeries.value().seats();
        }

        ~Wagon() {
            if (number()) in_service_--;
        }

    private:
        static inline constinit unique_id_t numCreated = 0;  // constinit nie jest potrzebne
        static inline constinit size_t in_service_ = 0;
        optional<WagonSeries> wagonSeries;
        unique_id_t number_ = 0;
    };

    class Locomotive {
    public:
        explicit Locomotive(string_view designation, size_t max_wagons)
            : max_wagons_(max_wagons) {
            if (designation.empty()) {
                designation_ = "?";
            } else {
                designation_ = designation;
            }
        }
        
        Locomotive() {}

        Locomotive(const Locomotive&) = delete;
        Locomotive& operator=(const Locomotive&) = delete;

        Locomotive& operator=(Locomotive&& other) noexcept {
            if (this != &other) {
                designation_ = std::move(other.designation_);
                max_wagons_ = other.max_wagons_;

                other.designation_.clear();
                other.max_wagons_ = 0;
            }
            return *this;
        }

        Locomotive(Locomotive&& other) noexcept {
            designation_ = std::move(other.designation_);
            max_wagons_ = other.max_wagons_;

            other.designation_.clear();
            other.max_wagons_ = 0;
        }

        const string& designation() const {
            return designation_;
        }
        size_t max_wagons() const {
            return max_wagons_;
        }

        explicit operator bool() const { return !designation_.empty(); }

        friend ostream& operator<<(ostream& os, const Locomotive& locomotive) {
            if (!locomotive) {
                os << "brak lokomotywy";
            } else {
                os << locomotive.designation_ << " (uciąg: " << locomotive.max_wagons_ << ")";
            }
            return os;
        }

    private:
        string designation_;
        size_t max_wagons_ = 0;
    };

    class Train {
    public:
        size_t size() const {
            return wagons.size();
        }
        size_t capacity() const {
            return locomotive_.max_wagons();
        }
        size_t seats() const {
            size_t result = 0;
            for (const Wagon& wagon : wagons) {
                result += wagon.seats();
            }
            return result;
        }
        Train() {}

        Train(const Train&) = delete;
        Train& operator=(const Train&) = delete;

        explicit Train(Locomotive&& locomotive, initializer_list<Wagon> wagon_list = {})
            : locomotive_(std::move(locomotive)) {
            for (const Wagon& wagon : wagon_list) {
                if (size() >= capacity()) break;
                if (wagon) {
                    wagons.push_back(wagon);  // teraz sie zwiekszaja indeksy, czy tak ma byc ?
                }
            }
        }

        Train& operator=(Train&& other) noexcept {
            if (this != &other) {  // zakladam, ze tak ma byc.
                locomotive_ = std::move(other.locomotive_);
                wagons = std::move(other.wagons);
                other.wagons.clear();
            }
            return *this;
        }

        Train(Train&& other) noexcept {
            locomotive_ = std::move(other.locomotive_);
            wagons = std::move(other.wagons);
            other.wagons.clear();
        }

        Train& operator+=(const Wagon& wagon) {
            if (wagon && size() < capacity()) {
                wagons.push_back(wagon);
            }
            return *this;
        }
        Train& operator+=(Wagon&& wagon) {
            if (wagon && size() < capacity()) {
                wagons.push_back(std::move(wagon));
            }
            return *this;
        }

        const Wagon& operator[](size_t i) const { return wagons[i]; }

        const Locomotive& locomotive() const { return locomotive_; }

        explicit operator bool() const { return static_cast<bool>(locomotive_); }

        friend ostream& operator<<(ostream& os, const Train& train) {
            os << "Skład: " << train.locomotive_;
            for (const Wagon& wagon : train.wagons) {
                os << "\n\t" << wagon;
            }
            return os;
        }

    private:
        Locomotive locomotive_;
        vector<Wagon> wagons;
    };
    
    class Depot {
        private:
            std::queue<Locomotive> locomotives_;
            std::vector<Wagon> wagons_;

        public:
            Depot() = default;
            Depot(const Depot&) = delete;
            Depot& operator =(const Depot&) = delete;

            Locomotive release_locomotive() {
                if (locomotives_.size() > 0) {
                    Locomotive tmp = std::move(locomotives_.front()); 
                    locomotives_.pop();
                    return tmp;
                }
                
                return Locomotive();
            }

            Wagon release_wagon(WagonSeries series_number) {        
                for (std::size_t i = 0; i < wagons_.size(); ++i) {
                    if (wagons_[i].series() == series_number) {
                        Wagon result = std::move(wagons_[i]);
                        
                        wagons_.erase(wagons_.begin() + i);
                        
                        return result;
                    }
                }

                return Wagon();
            }


            Depot& operator +=(Locomotive&& locomotive) {
                if (locomotive) locomotives_.push(std::move(locomotive));
                
                return *this;
            }

            Depot& operator +=(Wagon&& wagon) {
                if (wagon) wagons_.push_back(std::move(wagon));

                return *this;
            }

            std::size_t locomotives() const {
                return locomotives_.size();
            }

            std::size_t wagons() const {
                return wagons_.size();
            }

            std::size_t wagons(WagonSeries series_number) const {
                std::size_t count = 0;
                for (auto& w : wagons_ | std::views::reverse) {
                    if (w.series() == series_number) 
                        ++count;
                }
                return count;
            }
        };
        
    inline Depot& main_depot() {
        struct MainDepot : Depot {
            MainDepot() {
                *this += Locomotive{"EU07-001", 8};
                *this += Locomotive{"EP09-020", 12};
                *this += Wagon{STANDARD_FIRST_CLASS};
                for (int i = 0; i < 4; ++i) *this += Wagon{STANDARD_SECOND_CLASS};
                for (int i = 0; i < 2; ++i) *this += Wagon{STANDARD_SLEEPER};
                *this += Wagon{STANDARD_DINING};
            }
        };
        static MainDepot depot;
        return depot;
    }
    
    
    class Dispatcher {
        Depot& depot;
        public:
            explicit Dispatcher(Depot& dep) : depot(dep) {}
            Dispatcher() : depot(main_depot()) {}

            const Depot& yard() const {
                return depot;
            }

            Train assemble(const WagonSeries series, const std::size_t count) const {
                Train train{depot.release_locomotive()};
                size_t cnt = 0;
                while (depot.wagons(series) > 0 && cnt < count && train.capacity() > cnt) {
                train += depot.release_wagon(series);
                    ++cnt;
                }
                return train;
            }
    };

}  // namespace trains_details

using trains_details::WagonType;
using trains_details::WagonSeries;
using trains_details::STANDARD_FIRST_CLASS;
using trains_details::STANDARD_SECOND_CLASS;
using trains_details::STANDARD_SLEEPER;
using trains_details::STANDARD_DINING;
using trains_details::Wagon;
using trains_details::Locomotive;
using trains_details::Train;
using trains_details::Depot;
using trains_details::main_depot;
using trains_details::Dispatcher;

#endif
