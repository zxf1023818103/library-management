create database library_management;
go

use library_management;
go

create login app with password = 'Dcs&P^hT3(zM2j3';
go

create user app from login app;
go

alter role db_owner add member app;
go
