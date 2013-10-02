package org.jfno.jdbc;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SqlBuilderTest {

    @Test
    public void baseTest() {
        //Fluent Style
        String sql = new SqlBuilder()
                .SELECT("id, name").FROM("PERSON A")
                .WHERE("name like ?")
                .WHERE("id = ?").build();

        assertEquals("" +
                "SELECT id, name\n" +
                "FROM PERSON A\n" +
                "WHERE (name like ? AND id = ?)", sql);

        sql = new SqlBuilder() {{
            SELECT("id, name");
            FROM("PERSON A");
            WHERE("name like ?").WHERE("id = ?");
        }}.toString();

        //Mixed
        sql = new SqlBuilder() {{
            SELECT("id, name");
            FROM("PERSON A");
            WHERE("name like ?").WHERE("id = ?");
        }}.build();

        assertEquals("" +
                "SELECT id, name\n" +
                "FROM PERSON A\n" +
                "WHERE (name like ? AND id = ?)", sql);

        //You can pass in your own StringBuilder
        StringBuilder sb = new StringBuilder();
        //From the tutorial
        sql = new SqlBuilder() {{
            SELECT("P.ID, P.USERNAME, P.PASSWORD, P.FULL_NAME");
            SELECT("P.LAST_NAME, P.CREATED_ON, P.UPDATED_ON");
            FROM("PERSON P");
            FROM("ACCOUNT A");
            INNER_JOIN("DEPARTMENT D on D.ID = P.DEPARTMENT_ID");
            INNER_JOIN("COMPANY C on D.COMPANY_ID = C.ID");
            WHERE("P.ID = A.ID");
            WHERE("P.FIRST_NAME like ?");
            OR();
            WHERE("P.LAST_NAME like ?");
            GROUP_BY("P.ID");
            HAVING("P.LAST_NAME like ?");
            OR();
            HAVING("P.FIRST_NAME like ?");
            ORDER_BY("P.ID");
            ORDER_BY("P.FULL_NAME");
        }}.SQL(sb).toString();

        assertEquals("SELECT P.ID, P.USERNAME, P.PASSWORD, P.FULL_NAME, P.LAST_NAME, P.CREATED_ON, P.UPDATED_ON\n" +
                "FROM PERSON P, ACCOUNT A\n" +
                "INNER JOIN DEPARTMENT D on D.ID = P.DEPARTMENT_ID\n" +
                "INNER JOIN COMPANY C on D.COMPANY_ID = C.ID\n" +
                "WHERE (P.ID = A.ID AND P.FIRST_NAME like ?) \n" +
                "OR (P.LAST_NAME like ?)\n" +
                "GROUP BY P.ID\n" +
                "HAVING (P.LAST_NAME like ?) \n" +
                "OR (P.FIRST_NAME like ?)\n" +
                "ORDER BY P.ID, P.FULL_NAME", sql);

    }

    @Test
    public void SubQueryTest() {
        //Sub Query
        SqlBuilder sqlBuilder = new SqlBuilder()
            .SELECT("P.ID, P.USERNAME, P.PASSWORD, P.FULL_NAME")
            .SELECT("P.LAST_NAME, P.CREATED_ON, P.UPDATED_ON")
            .FROM("PERSON", " P")
            .WHERE("P.ID = ?")
            .ORDER_BY("P.LAST_NAME, ", "P.FIRST_NAME");
        sqlBuilder.subCondition()
                .WHERE("P.FIRST_NAME like ?")
                .OR()
                .WHERE("P.LAST_NAME like ?");

        String sql = sqlBuilder.build();

        assertEquals("SELECT P.ID, P.USERNAME, P.PASSWORD, P.FULL_NAME, P.LAST_NAME, P.CREATED_ON, P.UPDATED_ON\n" +
                "FROM PERSON P\n" +
                "WHERE (P.ID = ?) \n" +
                "AND ( (P.FIRST_NAME like ?) \n" +
                "OR (P.LAST_NAME like ?))\n" +
                "ORDER BY P.LAST_NAME, P.FIRST_NAME", sql);
    }


    @Test
    public void multiTableTest() {
        //Sub Query
        SqlBuilder sqlBuilder = new SqlBuilder()
                .addTable("PERSON", "P", "ID")
                .addTable("INFO", "I", "PERSON_ID")
                .SELECT("P.USERNAME, P.PASSWORD, P.FULL_NAME")
                .SELECT("P.LAST_NAME, P.CREATED_ON, P.UPDATED_ON")
                .WHERE("P.ID = ?")
                .WHERE("I.VALUE = ?")
                .ORDER_BY("P.LAST_NAME, ", "P.FIRST_NAME");
        sqlBuilder.subCondition()
                .WHERE("P.FIRST_NAME like ?")
                .OR()
                .WHERE("P.LAST_NAME like ?");

        String sql = sqlBuilder.build();

        assertEquals("SELECT P.ID, P.USERNAME, P.PASSWORD, P.FULL_NAME, P.LAST_NAME, P.CREATED_ON, P.UPDATED_ON\n" +
                "FROM PERSON P\n" +
                "INNER JOIN INFO I ON P.ID = I.PERSON_ID\n" +
                "WHERE (P.ID = ? AND I.VALUE = ?) \n" +
                "AND ( (P.FIRST_NAME like ?) \n" +
                "OR (P.LAST_NAME like ?))\n" +
                "ORDER BY P.LAST_NAME, P.FIRST_NAME", sql);
    }

}
