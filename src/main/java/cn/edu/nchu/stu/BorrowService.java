package cn.edu.nchu.stu;

import java.io.IOException;
import java.sql.*;
import java.util.List;
import java.util.Properties;

class BorrowService {

    public static final int FINE_NOT_PAID = -1;

    public static final int BORROW_NUMBER_EXCEED = -2;

    public static final int BOOK_BORROWED = -3;

    public static final int RECORD_NOT_FOUND = -4;

    public static final int BORROW_PERIOD_EXCEED = -5;

    private String url;

    private Connection connection;

    private double fine = 0;

    private void loadConfig() throws IOException {
        Properties properties = new Properties();
        properties.load(getClass().getResourceAsStream("/config.properties"));
        url = properties.getProperty("url");
    }

    BorrowService() throws IOException, SQLException {
        loadConfig();
        connection = DriverManager.getConnection(url);
    }

    boolean testConnection() throws SQLException {
        Statement statement = connection.createStatement();
        statement.execute("select 1 + 1 as result");
        ResultSet resultSet = statement.getResultSet();
        if (resultSet.next()) {
            return resultSet.getInt("result") == 2;
        }
        else {
            return false;
        }
    }

    void initializeDatabase() throws SQLException {
        Statement statement = connection.createStatement();
        statement.execute("if object_id('Student', 'u') is null\n" +
                "    create table Student (\n" +
                "        name varchar(32) not null,\n" +
                "        department varchar(32) not null,\n" +
                "        major varchar(32) not null,\n" +
                "        maxBorrowNumber int not null default 5,\n" +
                "        id int primary key not null identity(10000,1),\n" +
                "        fine smallmoney not null default 0,\n" +
                "        maxBorrowPeriodDay int not null default 45,\n" +
                "        finePerDay smallmoney not null default 0.02\n" +
                "    );");
        statement.execute("if object_id('Book', 'u') is null\n" +
                "    create table Book (\n" +
                "        id int not null primary key identity(10000,1),\n" +
                "        title varchar(128) not null,\n" +
                "        press varchar(128) not null,\n" +
                "        isbn bigint not null,\n" +
                "        author varchar(32) not null,\n" +
                "        klass varchar(128) not null,\n" +
                "        publicationDate date not null,\n" +
                "        location varchar(32) not null,\n" +
                "        returned bit not null default 1\n" +
                "    );");
        statement.execute("if object_id('BorrowRecord', 'u') is null\n" +
                "    create table BorrowRecord (\n" +
                "        beginDate date not null default getdate(),\n" +
                "        id int primary key not null identity(1,1),\n" +
                "        studentId int not null,\n" +
                "        bookId int not null,\n" +
                "        returned bit not null default 0,\n" +
                "        constraint fk_Student_id foreign key (studentId) references Student(id) on delete cascade on update cascade,\n" +
                "        constraint fk_Book_id foreign key (bookId) references Book(id) on delete cascade on update cascade\n" +
                "    );");
        statement.execute("if object_id('borrowBook', 'p') is not null\n" +
                "    drop procedure borrowBook;");
        statement.execute("create procedure borrowBook @studentId int, @bookId int as\n" +
                "begin\n" +
                "    declare @fine smallmoney;\n" +
                "    select @fine = fine from Student where id = @studentId;\n" +
                "    if @fine <> 0\n" +
                "        --- 未缴清罚款\n" +
                "        return -1;\n" +
                "    declare @borrowedNumber int, @maxBorrowedNumber int;\n" +
                "    select @borrowedNumber = count(*) from BorrowRecord where studentId = @studentId and returned = 0;\n" +
                "    select @maxBorrowedNumber = maxBorrowNumber from Student where id = @studentId;\n" +
                "    if @borrowedNumber > @maxBorrowedNumber\n" +
                "        --- 超过最大借阅数量\n" +
                "        return -2;\n" +
                "    if not exists(select * from Book where id = @bookId)\n" +
                "        --- 图书不存在\n" +
                "        return -4;\n" +
                "    if exists(select * from BorrowRecord where bookId = @bookId and returned = 0)\n" +
                "        --- 该书已借出\n" +
                "        return -3;\n" +
                "    insert into BorrowRecord (studentId, bookId) values (@studentId, @bookId);\n" +
                "    update Book set returned = 0 where id = @bookId;\n" +
                "    return 0;\n" +
                "end;");
        statement.execute("if object_id('returnBook', 'p') is not null\n" +
                "    drop procedure returnBook;");
        statement.execute("create procedure returnBook @bookId int, @fine smallmoney output as\n" +
                "begin\n" +
                "    set @fine = 0;\n" +
                "    declare @studentId int;\n" +
                "    select @studentId = studentId from BorrowRecord where bookId = @bookId and returned = 0;\n" +
                "    if @studentId is null\n" +
                "        --- 借阅记录不存在\n" +
                "        return -4;\n" +
                "    declare @borrowedDay int;\n" +
                "    declare @maxBorrowPeriodDay int;\n" +
                "    select @borrowedDay = datediff(day, beginDate, getdate()) from BorrowRecord where studentId = @studentId and id = @bookId;\n" +
                "    select @maxBorrowPeriodDay = maxBorrowPeriodDay from Student where id = @studentId;\n" +
                "    update BorrowRecord set returned = 1 where bookId = @bookId and returned = 0;\n" +
                "    update Book set returned = 1 where id = @bookId;\n" +
                "    if @borrowedDay > @maxBorrowPeriodDay\n" +
                "    begin\n" +
                "        select @fine = finePerDay * @borrowedDay from Student where id = @studentId;\n" +
                "        update Student set fine = @fine where id = @studentId;\n" +
                "        --- 产生了罚款\n" +
                "        return -5;\n" +
                "    end;\n" +
                "    return 0;\n" +
                "end;");
        statement.execute("if object_id('updateFine', 'p') is not null\n" +
                "    drop procedure updateFine;");
        statement.execute("create procedure updateFine @studentId int as\n" +
                "    update Student set fine = t.fine from (select sum((datediff(day, beginDate, getdate()) - maxBorrowPeriodDay) * finePerDay) as fine from Student join BorrowRecord BR on Student.id = BR.studentId where datediff(day, beginDate, getdate()) > maxBorrowPeriodDay and studentId = @studentId group by studentId) as t where id = @studentId;");
    }

    int borrowBook(int studentId, int bookId) throws SQLException {
        CallableStatement statement = connection.prepareCall("{ ? = call borrowBook(?, ?) }");
        statement.registerOutParameter(1, Types.INTEGER);
        statement.setInt(2, studentId);
        statement.setInt(3, bookId);
        statement.execute();
        return statement.getInt(1);
    }

    int returnBook(int bookId) throws SQLException {
        CallableStatement statement = connection.prepareCall("{ ? = call returnBook(?, ?) }");
        statement.registerOutParameter(1, Types.INTEGER);
        statement.setInt(2, bookId);
        statement.registerOutParameter(3, Types.DOUBLE);
        statement.execute();
        fine = statement.getDouble(3);
        return statement.getInt(1);
    }

    double getFine() {
        return fine;
    }

    double getFine(int studentId) throws SQLException {
        updateFine(studentId);
        Statement statement = connection.createStatement();
        statement.execute("select fine from Student where id = " + studentId);
        ResultSet resultSet = statement.getResultSet();
        resultSet.next();
        return resultSet.getDouble(0);
    }

    private void updateFine(int studentId) throws SQLException {
        Statement statement = connection.createStatement();
        statement.execute("execute updateFine " + studentId);
    }

    int payFine(int studentId) throws SQLException {
        Statement statement = connection.createStatement();
        statement.execute("update Student set fine = 0 where id = " + studentId);
        if (statement.getUpdateCount() == 0) {
            return RECORD_NOT_FOUND;
        }
        return 0;
    }

    List<Book> findBookByTitle(String title) throws SQLException {
        Statement statement = connection.createStatement();
        statement.execute("select * from Book where title like \'%" + title + "%\';");
        ResultSet resultSet = statement.getResultSet();
        return Book.loadFromResultSet(resultSet);
    }

    int getBorrowedNumber(int studentId) throws SQLException {
        Statement statement = connection.createStatement();
        statement.execute("select count(*) from BorrowRecord where studentId = " + studentId + " and returned = 0;");
        ResultSet resultSet = statement.getResultSet();
        resultSet.next();
        return resultSet.getInt(1);
    }

    int getMaxBorrowNumber(int studentId) throws SQLException {
        Statement statement = connection.createStatement();
        statement.execute("select maxBorrowNumber from Student where id = " + studentId);
        ResultSet resultSet = statement.getResultSet();
        resultSet.next();
        return resultSet.getInt(1);
    }

    Student addStudent(String name, String department, String major) throws SQLException {
        Statement statement = connection.createStatement();
        statement.execute("insert into Student(name, department, major) values (\'"+ name + "\', \'" + department + "\', \'" + major + "\');");
        statement.execute("select * from Student where id = scope_identity();");
        ResultSet resultSet = statement.getResultSet();
        List<Student> students = Student.loadFromResultSet(resultSet);
        return students.get(0);
    }
}
