package cn.edu.nchu.stu;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Book {

    private int id;

    private String title;

    private String press;

    private String isbn;

    private String author;

    private String klass;

    private String publicationDate;

    private String location;

    public static List<Book> loadFromResultSet(ResultSet resultSet) throws SQLException {
        List<Book> result = new ArrayList<Book>();
        while (resultSet.next()) {
            Book book = new Book();
            book.id = resultSet.getInt("id");
            book.title = resultSet.getString("title");
            book.press = resultSet.getString("press");
            book.isbn = resultSet.getString("isbn");
            book.author = resultSet.getString("author");
            book.klass = resultSet.getString("klass");
            book.publicationDate = resultSet.getString("publicationDate");
            book.location = resultSet.getString("location");
            result.add(book);
        }
        return result;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPress() {
        return press;
    }

    public void setPress(String press) {
        this.press = press;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getKlass() {
        return klass;
    }

    public void setKlass(String klass) {
        this.klass = klass;
    }

    public String getPublicationDate() {
        return publicationDate;
    }

    public void setPublicationDate(String publicationDate) {
        this.publicationDate = publicationDate;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
