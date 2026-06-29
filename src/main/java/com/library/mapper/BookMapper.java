package com.library.mapper;

import com.library.entity.Book;
import com.library.entity.Category;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface BookMapper {

    @Select("SELECT b.*, c.name as category_name, c.`desc` as category_desc FROM books b LEFT JOIN categories c ON b.categoryId = c.id WHERE b.id = #{id}")
    @Results(id = "bookResult", value = {
        @Result(property = "id", column = "id"),
        @Result(property = "categoryId", column = "categoryId"),
        @Result(property = "category", column = "categoryId",
            javaType = Category.class,
            one = @One(select = "com.library.mapper.CategoryMapper.findById"))
    })
    Book findById(Integer id);

    @Select("SELECT * FROM books WHERE isbn = #{isbn}")
    Book findByIsbn(String isbn);

    @Select("<script>" +
            "SELECT b.* FROM books b LEFT JOIN categories c ON b.categoryId = c.id " +
            "WHERE 1=1 " +
            "<if test='search != null'>AND (b.title LIKE CONCAT('%', #{search}, '%') OR b.author LIKE CONCAT('%', #{search}, '%') OR b.isbn LIKE CONCAT('%', #{search}, '%'))</if> " +
            "<if test='categoryId != null'>AND b.categoryId = #{categoryId}</if> " +
            "<if test='yearMin != null'>AND b.year &gt;= #{yearMin}</if> " +
            "<if test='yearMax != null'>AND b.year &lt;= #{yearMax}</if> " +
            "<if test='language != null'>AND b.language = #{language}</if> " +
            "<if test='campus != null'>AND b.id IN (SELECT bookId FROM book_items WHERE campus = #{campus})</if> " +
            "ORDER BY <choose>" +
            "<when test='sortBy == \"year\"'>b.year DESC</when>" +
            "<when test='sortBy == \"title\"'>b.title ASC</when>" +
            "<otherwise>b.created_at DESC</otherwise>" +
            "</choose> " +
            "</script>")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "category", column = "categoryId",
            javaType = Category.class,
            one = @One(select = "com.library.mapper.CategoryMapper.findById"))
    })
    List<Book> searchBooks(Map<String, Object> params);

    @Select("<script>" +
            "SELECT COUNT(*) FROM books b LEFT JOIN categories c ON b.categoryId = c.id " +
            "WHERE 1=1 " +
            "<if test='search != null'>AND (b.title LIKE CONCAT('%', #{search}, '%') OR b.author LIKE CONCAT('%', #{search}, '%') OR b.isbn LIKE CONCAT('%', #{search}, '%'))</if> " +
            "<if test='categoryId != null'>AND b.categoryId = #{categoryId}</if> " +
            "<if test='yearMin != null'>AND b.year &gt;= #{yearMin}</if> " +
            "<if test='yearMax != null'>AND b.year &lt;= #{yearMax}</if> " +
            "<if test='language != null'>AND b.language = #{language}</if> " +
            "</script>")
    long countBooks(Map<String, Object> params);

    @Insert("INSERT INTO books(isbn, title, author, publisher, year, total, available, status, location, cover, `desc`, clcNumber, physicalDesc, language, country, categoryId, created_at, updated_at) " +
            "VALUES(#{isbn}, #{title}, #{author}, #{publisher}, #{year}, #{total}, #{available}, #{status}, #{location}, #{cover}, #{desc}, #{clcNumber}, #{physicalDesc}, #{language}, #{country}, #{categoryId}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Book book);

    @Update("<script>" +
            "UPDATE books SET updated_at=NOW()" +
            "<if test='isbn != null'>, isbn=#{isbn}</if>" +
            "<if test='title != null'>, title=#{title}</if>" +
            "<if test='author != null'>, author=#{author}</if>" +
            "<if test='publisher != null'>, publisher=#{publisher}</if>" +
            "<if test='year != null'>, year=#{year}</if>" +
            "<if test='total != null'>, total=#{total}</if>" +
            "<if test='location != null'>, location=#{location}</if>" +
            "<if test='cover != null'>, cover=#{cover}</if>" +
            "<if test='desc != null'>, `desc`=#{desc}</if>" +
            "<if test='clcNumber != null'>, clcNumber=#{clcNumber}</if>" +
            "<if test='physicalDesc != null'>, physicalDesc=#{physicalDesc}</if>" +
            "<if test='language != null'>, language=#{language}</if>" +
            "<if test='country != null'>, country=#{country}</if>" +
            "<if test='categoryId != null'>, categoryId=#{categoryId}</if>" +
            " WHERE id=#{id}</script>")
    void update(Book book);

    @Update("UPDATE books SET total=#{total}, available=#{available} WHERE id=#{id}")
    void updateTotalAndAvailable(@Param("id") Integer id, @Param("total") Integer total, @Param("available") Integer available);

    @Update("UPDATE books SET available = available - 1 WHERE id=#{id} AND available > 0")
    int decrementAvailable(Integer id);

    @Update("UPDATE books SET available = available + 1 WHERE id=#{id}")
    int incrementAvailable(Integer id);

    @Update("UPDATE books SET status = #{status}, updated_at = NOW() WHERE id = #{id}")
    void updateStatus(@Param("id") Integer id, @Param("status") String status);

    @Delete("DELETE FROM books WHERE id=#{id}")
    void deleteById(Integer id);

    @Select("SELECT COUNT(*) FROM books")
    long count();

    @Select("SELECT * FROM books ORDER BY created_at DESC")
    List<Book> findAll();

    @Select("SELECT DISTINCT language FROM books WHERE language IS NOT NULL ORDER BY language")
    List<String> findLanguages();

    @Select("SELECT DISTINCT year FROM books WHERE year IS NOT NULL ORDER BY year")
    List<Integer> findYears();
}
