This is a SqlBuilder inspired from the org.apache.ibatis.jdbc.AbstractSQL SQL query builder of [MyBatis](https://github.com/mybatis/mybatis-3/).

I needed to have a way to simply insert ORs that were in parenthesis to make sure they took precedence over the surrounding ANDs. You can look at the tests for sample way of using it.
