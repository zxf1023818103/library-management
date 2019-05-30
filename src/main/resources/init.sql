if object_id('Student', 'u') is null
    create table Student (
        name varchar(32) not null,
        department varchar(32) not null,
        major varchar(32) not null,
        maxBorrowNumber int not null default 5,
        id int primary key not null identity(10000,1),
        fine smallmoney not null default 0,
        maxBorrowPeriodDay int not null default 45,
        finePerDay smallmoney not null default 0.02
    );
go

if object_id('Book', 'u') is null
    create table Book (
        id int not null primary key identity(10000,1),
        title varchar(128) not null,
        press varchar(128) not null,
        isbn bigint not null,
        author varchar(32) not null,
        klass varchar(128) not null,
        publicationDate date not null,
        location varchar(32) not null,
        returned bit not null default 1
    );
go

if object_id('BorrowRecord', 'u') is null
    create table BorrowRecord (
        beginDate date not null default getdate(),
        id int primary key not null identity(1,1),
        studentId int not null,
        bookId int not null,
        returned bit not null default 0,
        constraint fk_Student_id foreign key (studentId) references Student(id) on delete cascade on update cascade,
        constraint fk_Book_id foreign key (bookId) references Book(id) on delete cascade on update cascade
    );
go

if object_id('borrowBook', 'p') is not null
    drop procedure borrowBook;

create procedure borrowBook @studentId int, @bookId int as
begin
    declare @fine smallmoney;
    select @fine = fine from Student where id = @studentId;
    if @fine <> 0
        --- 未缴清罚款
        return -1;
    declare @borrowedNumber int, @maxBorrowedNumber int;
    select @borrowedNumber = count(*) from BorrowRecord where studentId = @studentId and returned = 0;
    select @maxBorrowedNumber = maxBorrowNumber from Student where id = @studentId;
    if @borrowedNumber > @maxBorrowedNumber
        --- 超过最大借阅数量
        return -2;
    if not exists(select * from Book where id = @bookId)
        --- 图书不存在
        return -4;
    if exists(select * from BorrowRecord where bookId = @bookId and returned = 0)
        --- 该书已借出
        return -3;
    insert into BorrowRecord (studentId, bookId) values (@studentId, @bookId);
    update Book set returned = 0 where id = @bookId;
    return 0;
end;
go

if object_id('updateFine', 'p') is not null
    drop procedure updateFine;
go

create procedure updateFine @studentId int as
    update Student set fine = t.fine from (select sum((datediff(day, beginDate, getdate()) - maxBorrowPeriodDay) * finePerDay) as fine from Student join BorrowRecord BR on Student.id = BR.studentId where datediff(day, beginDate, getdate()) > maxBorrowPeriodDay and studentId = @studentId group by studentId) as t where id = @studentId;
go

if object_id('returnBook', 'p') is not null
    drop procedure returnBook;
go

create procedure returnBook @bookId int, @fine smallmoney output as
begin
    set @fine = 0;
    declare @studentId int;
    select @studentId = studentId from BorrowRecord where bookId = @bookId and returned = 0;
    if @studentId is null
        --- 借阅记录不存在
        return -4;
    declare @borrowedDay int;
    declare @maxBorrowPeriodDay int;
    select @borrowedDay = datediff(day, beginDate, getdate()) from BorrowRecord where studentId = @studentId and id = @bookId;
    select @maxBorrowPeriodDay = maxBorrowPeriodDay from Student where id = @studentId;
    update BorrowRecord set returned = 1 where bookId = @bookId and returned = 0;
    update Book set returned = 1 where id = @bookId;
    if @borrowedDay > @maxBorrowPeriodDay
    begin
        select @fine = finePerDay * @borrowedDay from Student where id = @studentId;
        update Student set fine = @fine where id = @studentId;
        --- 产生了罚款
        return -5;
    end;
    return 0;
end;
go
