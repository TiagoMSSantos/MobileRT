#include <gtest/gtest.h>

::std::int32_t main(::std::int32_t argc, char **argv) {
  ::testing::InitGoogleTest(&argc, argv);
  const ::std::int32_t res {RUN_ALL_TESTS()};
	::std::cin.ignore();
	return res;
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
ASSERT_DEATH(statement, regex);					EXPECT_DEATH(statement, regex);		statement crashes with the given error
ASSERT_DEATH_IF_SUPPORTED(statement, regex);	EXPECT_DEATH_IF_SUPPORTED(statement, regex);	if death tests are supported, verifies that statement crashes with the given error; otherwise verifies nothing
ASSERT_EXIT(statement, predicate, regex);		EXPECT_EXIT(statement, predicate, regex);	statement exits with the given error and its exit code matches predicate
*/
