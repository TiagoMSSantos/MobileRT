#include <gtest/gtest.h>
#include <MobileRT/Utils/Constants.hpp>
#include <MobileRT/Utils/Utils.hpp>
#include <array>
#if __cplusplus >= 202002L
    #include <latch>
#endif
#include <thread>
#include <vector>
#include <mutex>

// Only execute test if C++20 or later is supported, as it uses `std::latch` which is not available in C++17.
#if __cplusplus >= 202002L

namespace {
    ::std::array<float, ::MobileRT::ArraySize> randomSequence {};
}//namespace

/**
 * Helper method which tests the concurrency of a function by running it in multiple threads.
 *
 * @param func The function to test for concurrency.
 */
template<typename T, ::std::size_t S>
inline void testConcurrencyOfFunction(const ::std::function<void(::std::array<T, S>*)> &func) {
    const ::std::size_t NUM_THREADS {::std::thread::hardware_concurrency() + 1};
    const ::std::size_t ITERATIONS_PER_THREAD {20};

    ASSERT_GT(NUM_THREADS, 1) << "Number of threads should be greater than 1 for this test to validate thread safety";
    ASSERT_GT(S, 1'000) << "Array size should be greater than 1'000 for this test to be meaningful";
    ASSERT_LE(S, 0xFFFFF + 1) << "Array size should not be greater than 0xFFFFF + 1, so the mask can be used when getting the index in the array";
    ASSERT_GT(ITERATIONS_PER_THREAD, 1) << "Iterations per thread should be greater than 1 for this test to be meaningful";

    ::std::vector<::std::thread> threads {};
    threads.reserve(NUM_THREADS);
    ::std::latch latch {static_cast<long int> (NUM_THREADS)};

    const auto threadFunc {[&]() {
        // std::array size needs to fit in the stack.
        ::std::vector<::std::array<T, S>> allResults {};
        allResults.reserve(ITERATIONS_PER_THREAD);
        ::std::cout << "New thread waiting" << ::std::endl << ::std::flush;
        latch.arrive_and_wait();
        ::std::cout << "Started thread" << ::std::endl << ::std::flush;
        for (::std::size_t i {0}; i < ITERATIONS_PER_THREAD; ++i) {
            func(&randomSequence);
            allResults.emplace_back(randomSequence);
        }
        ::std::cout << "Checking that all generated numbers values within valid range [0, 1]" << ::std::endl << ::std::flush;
        for (const auto &result : allResults) {
            for (const float value : result) {
                ASSERT_GE(value, 0) << "Generated value is less than 0";
                ASSERT_LE(value, 1) << "Generated value is greater than 1";
            }
        }
    }};

    ::std::cout << "Starting " << NUM_THREADS << " threads" << ::std::endl << ::std::flush;
    for (::std::size_t i {0}; i < NUM_THREADS; ++i) {
        threads.emplace_back(threadFunc);
    }
    ::std::cout << "Waiting for " << NUM_THREADS << " threads to complete" << ::std::endl << ::std::flush;
    for (::std::thread &thread : threads) {
        ::std::cout << "Joining thread " << ::std::endl << ::std::flush;
        thread.join();
    }
    ::std::cout << "All " << NUM_THREADS << " threads completed" << ::std::endl << ::std::flush;
}

TEST(TestUtils, TestFillArrayWithHaltonSeqIsThreadSafe) {
    ::std::cout << "Starting test TestFillArrayWithHaltonSeqIsThreadSafe" << ::std::endl << ::std::flush;
    const ::std::function<void(::std::array<float, ::MobileRT::ArraySize>*)> func {::MobileRT::fillArrayWithHaltonSeq<float, ::MobileRT::ArraySize>};
    testConcurrencyOfFunction(func);
    ::std::cout << "Finished test TestFillArrayWithHaltonSeqIsThreadSafe" << ::std::endl << ::std::flush;
}

TEST(TestUtils, TestFillArrayWithMersenneTwisterIsThreadSafe) {
    ::std::cout << "Starting test TestFillArrayWithMersenneTwisterIsThreadSafe" << ::std::endl << ::std::flush;
    const ::std::function<void(::std::array<float, ::MobileRT::ArraySize>*)> func {::MobileRT::fillArrayWithMersenneTwister<float, ::MobileRT::ArraySize>};
    testConcurrencyOfFunction(func);
    ::std::cout << "Finished test TestFillArrayWithMersenneTwisterIsThreadSafe" << ::std::endl << ::std::flush;
}

TEST(TestUtils, TestFillArrayWithPCGIsThreadSafe) {
    ::std::cout << "Starting test TestFillArrayWithPCGIsThreadSafe" << ::std::endl << ::std::flush;
    const ::std::function<void(::std::array<float, ::MobileRT::ArraySize>*)> func {::MobileRT::fillArrayWithPCG<float, ::MobileRT::ArraySize>};
    testConcurrencyOfFunction(func);
    ::std::cout << "Finished test TestFillArrayWithPCGIsThreadSafe" << ::std::endl << ::std::flush;
}

#endif
