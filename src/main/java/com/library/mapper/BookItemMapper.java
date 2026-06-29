package com.library.mapper;

import com.library.entity.BookItem;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface BookItemMapper {

    @Select("SELECT * FROM book_items WHERE id = #{id}")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "itemType", column = "itemTypeId",
            one = @One(select = "com.library.mapper.ItemTypeMapper.findById"))
    })
    BookItem findById(Integer id);

    @Select("SELECT * FROM book_items WHERE barcode = #{barcode}")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "book", column = "bookId",
            one = @One(select = "com.library.mapper.BookMapper.findById")),
        @Result(property = "itemType", column = "itemTypeId",
            one = @One(select = "com.library.mapper.ItemTypeMapper.findById"))
    })
    BookItem findByBarcode(String barcode);

    @Select("SELECT * FROM book_items WHERE bookId = #{bookId} ORDER BY barcode ASC")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "itemType", column = "itemTypeId",
            one = @One(select = "com.library.mapper.ItemTypeMapper.findById"))
    })
    List<BookItem> findByBookId(Integer bookId);

    @Select("SELECT * FROM book_items WHERE bookId = #{bookId} AND status = 'available' LIMIT 1")
    BookItem findFirstAvailableByBookId(Integer bookId);

    @Select("SELECT * FROM book_items WHERE bookId = #{bookId} AND status = 'available'")
    List<BookItem> findAvailableByBookId(Integer bookId);

    @Select("SELECT COUNT(*) FROM book_items WHERE bookId = #{bookId} AND status = 'available'")
    long countAvailableByBookId(Integer bookId);

    @Select("SELECT COUNT(*) FROM book_items WHERE bookId = #{bookId}")
    long countByBookId(Integer bookId);

    @Select("SELECT DISTINCT campus FROM book_items WHERE campus IS NOT NULL ORDER BY campus")
    List<String> findCampuses();

    @Insert("INSERT INTO book_items(barcode, callNumber, location, `condition`, status, price, acquired_at, notes, campus, requests, bookId, itemTypeId, created_at, updated_at) " +
            "VALUES(#{barcode}, #{callNumber}, #{location}, #{condition}, #{status}, #{price}, #{acquiredAt}, #{notes}, #{campus}, #{requests}, #{bookId}, #{itemTypeId}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(BookItem item);

    @Update("UPDATE book_items SET status=#{status}, updated_at=NOW() WHERE id=#{id}")
    int updateStatus(@Param("id") Integer id, @Param("status") String status);

    @Update("UPDATE book_items SET requests = requests + 1 WHERE id=#{id}")
    void incrementRequests(Integer id);
}
