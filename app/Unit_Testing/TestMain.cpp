#include <gtest/gtest.h>
#include "MobileRT/Utils/Utils.hpp"
#include <iostream>

::std::int32_t main (::std::int32_t argc, char **argv) {
    // Only catch signals for Linux systems, since boost stacktrace doesn't work on Windows nor MacOS.
    #if !defined(_WIN32) && !defined(__APPLE__)
        ::std::cout << "Setting up signals catch." << ::std::endl;
        ::std::signal(SIGSEGV, ::MobileRT::signalHandler);
        ::std::signal(SIGKILL, ::MobileRT::signalHandler);
    #endif

    ::std::cout << "Starting unit tests." << ::std::endl;
    ::testing::InitGoogleTest(&argc, argv);
    ::std::cout << "Initialized GoogleTest." << ::std::endl;
    const ::std::int32_t res {RUN_ALL_TESTS()};
    ::std::cout << "Executed unit tests." << ::std::endl;
    if (::testing::UnitTest::GetInstance()->test_to_run_count() <= 0) {
        ::std::cerr << "No test was executed." << ::std::endl;
        return 1;
    }
    if (::testing::UnitTest::GetInstance()->successful_test_count() <= 0) {
        ::std::cerr << "No test passed." << ::std::endl;
        return 1;
    }
    if (res != 0 || ::testing::UnitTest::GetInstance()->failed_test_count() != 0) {
        ::std::cerr << "Some tests failed." << ::std::endl;
        return 1;
    }
    ::std::cout << "All tests passed!" << ::std::endl;
    return 0;
}

/*
						Basic Assertions
Fatal assertion						Nonfatal assertion					Verifies
ASSERT_TRUE(condition);				EXPECT_TRUE(condition);				condition is true
ASSERT_FALSE(condition);			EXPECT_FALSE(condition);			condition is false


						Binary Comparison
Fatal assertion						Nonfatal assertion					Verifies
ASSERT_EQ(val1,val2);				EXPECT_EQ(val1,val2);				val1 == val2
ASSERT_NE(val1,val2);				EXPECT_NE(val1,val2);				val1 != val2
ASSERT_LT(val1,val2);				EXPECT_LT(val1,val2);				val1 < val2
ASSERT_LE(val1,val2);				EXPECT_LE(val1,val2);				val1 <= val2
ASSERT_GT(val1,val2);				EXPECT_GT(val1,val2);				val1 > val2
ASSERT_GE(val1,val2);				EXPECT_GE(val1,val2);				val1 >= val2


						String Comparison
Fatal assertion						Nonfatal assertion					Verifies
ASSERT_STREQ(str1,str2);			EXPECT_STREQ(str1,_str_2);			the two C strings have the same content
ASSERT_STRNE(str1,str2);			EXPECT_STRNE(str1,str2);			the two C strings have different content
ASSERT_STRCASEEQ(str1,str2);		EXPECT_STRCASEEQ(str1,str2);		the two C strings have the same content, ignoring case
ASSERT_STRCASENE(str1,str2);		EXPECT_STRCASENE(str1,str2);		the two C strings have different content, ignoring case


						Floating-Point Macros
Fatal assertion						Nonfatal assertion					Verifies
ASSERT_FLOAT_EQ(val1, val2);		EXPECT_FLOAT_EQ(val1, val2);		the two float values are almost equal
ASSERT_DOUBLE_EQ(val1, val2);		EXPECT_DOUBLE_EQ(val1, val2);		the two double values are almost equal
ASSERT_NEAR(val1, val2, abs_error);	EXPECT_NEAR(val1, val2, abs_error);	the difference between val1 and val2 doesn't exceed the given absolute error



						How to Write a Death Test
Fatal assertion									Nonfatal assertion					Verifies
ASSERT_DEATH(statement, regex);					EXPECT_DEATH(statement, regex);		            statement crashes with the given error
ASSERT_DEATH_IF_SUPPORTED(statement, regex);	EXPECT_DEATH_IF_SUPPORTED(statement, regex);    if death tests are supported, verifies that statement crashes with the given error; otherwise verifies nothing
ASSERT_EXIT(statement, predicate, regex);		EXPECT_EXIT(statement, predicate, regex);	    statement exits with the given error and its exit code matches predicate
*/
