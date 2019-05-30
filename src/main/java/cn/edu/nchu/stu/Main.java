package cn.edu.nchu.stu;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Stack;

public class Main extends Application {

    public HBox menuPanel;
    public HBox studentCreationPanel;
    public HBox bookBorrowPanel;
    public VBox bookSearchPanel;
    public TextField nameTextField;
    public TextField departmentTextField;
    public TextField majorTextField;
    public Label createResultLabel;
    public TextField borrowBookIdTextField;
    public Label borrowResultLabel;
    public TextField keywordTextField;
    public TableView<Book> searchResultTableView;
    public HBox bookReturnPanel;
    public TextField returnBookIdTextField;
    public Label returnResultLabel;
    public Label borrowedNumberLabel;
    public Label remainingBorrowQuotaLabel;
    public Label fineLabel;
    public HBox finPaymentPanel;
    public TextField borrowBookStudentIdTextField;
    public TextField finePaymentStudentIdTextField;
    public Label finePaymentResultLabel;
    public Button createStudentButton;
    public Button borrowBookButton;
    public Button returnBookButton;
    public Button payFineButton;

    public Main() throws IOException, SQLException {
    }

    @Override
    public void start(Stage primaryStage) throws IOException, SQLException {
        Parent parent = FXMLLoader.load(getClass().getResource("/main.fxml"));
        primaryStage.setScene(new Scene(parent));
        primaryStage.setTitle("图书馆管理子系统");
        borrowService.initializeDatabase();
        primaryStage.show();
    }

    private Stack<Pane> historyPaneStack = new Stack<>();

    private BorrowService borrowService = new BorrowService();

    /**
     * 后退到上一个页面
     */
    public void locateBack(ActionEvent event) {
        if (historyPaneStack.size() <= 1)
            return;
        historyPaneStack.pop().setVisible(false);
        historyPaneStack.peek().setVisible(true);
    }

    /**
     * 跳转到指定页面
     * @param panel 要跳转到的页面
     */
    private void locateTo(Pane panel) {
        if (historyPaneStack.empty())
            historyPaneStack.push(menuPanel);
        historyPaneStack.peek().setVisible(false);
        panel.setVisible(true);
        historyPaneStack.push(panel);
    }

    public void locateToStudentCreationPanel(ActionEvent actionEvent) {
        locateTo(studentCreationPanel);
    }

    public void locateToBookSearchPanel(ActionEvent actionEvent) {
        locateTo(bookSearchPanel);
    }

    public void locateToBookBorrowPanel(ActionEvent actionEvent) {
        locateTo(bookBorrowPanel);
    }

    public void locateToBookReturnPanel(ActionEvent actionEvent) {
        locateTo(bookReturnPanel);
    }

    public void locateToFinePaymentPanel(ActionEvent actionEvent) {
        locateTo(finPaymentPanel);
    }

    public void locateBackMenu(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ESCAPE) {
            locateBack(null);
        }
    }

    public void createStudent(ActionEvent actionEvent) {
        createStudentButton.setDisable(true);
        createResultLabel.setTextFill(Color.RED);
        if (nameTextField.getText().isEmpty()) {
            createResultLabel.setText("请输入姓名");
        }
        else if (departmentTextField.getText().isEmpty()) {
            createResultLabel.setText("请输入系别");
        }
        else if (majorTextField.getText().isEmpty()) {
            createResultLabel.setText("请输入专业");
        }
        else {
            try {
                Student student = borrowService.addStudent(nameTextField.getText(), departmentTextField.getText(), majorTextField.getText());
                createResultLabel.setTextFill(Color.GREEN);
                createResultLabel.setText("办理成功。借书证号：" + student.getId());
            } catch (SQLException e) {
                createResultLabel.setText(e.getLocalizedMessage());
            }
        }
        createStudentButton.setDisable(false);
    }

    public void refreshSearchResult(KeyEvent keyEvent) throws SQLException {
        if (keyEvent.getCode() == KeyCode.ENTER) {
            List<Book> books = borrowService.findBookByTitle(keywordTextField.getText());
            searchResultTableView.getItems().addAll(books);
        }
    }

    public void borrowBook(ActionEvent actionEvent) {
        borrowBookButton.setDisable(true);
        borrowResultLabel.setTextFill(Color.RED);
        if (borrowBookStudentIdTextField.getText().isEmpty()) {
            borrowResultLabel.setText("请输入借书证号");
        }
        else if (borrowBookIdTextField.getText().isEmpty()) {
            borrowResultLabel.setText("请输入要借阅的书籍编号");
        }
        else {
            try {
                int ret = borrowService.borrowBook(Integer.parseInt(borrowBookStudentIdTextField.getText()), Integer.parseInt(borrowBookIdTextField.getText()));
                switch (ret) {
                    case BorrowService.RECORD_NOT_FOUND:
                        borrowResultLabel.setText("找不到此图书");
                        break;
                    case BorrowService.BORROW_NUMBER_EXCEED:
                        borrowResultLabel.setText("超出最大借阅数量");
                        break;
                    case BorrowService.FINE_NOT_PAID:
                        borrowResultLabel.setText("未缴清罚款");
                        break;
                    case BorrowService.BOOK_BORROWED:
                        borrowResultLabel.setText("该图书已被借阅");
                        break;
                    default:
                        borrowResultLabel.setTextFill(Color.GREEN);
                        borrowResultLabel.setText("借阅成功");
                        break;
                }
            } catch (SQLException e) {
                borrowResultLabel.setText(e.getLocalizedMessage());
            }
        }
        borrowBookButton.setDisable(false);
    }

    public void returnBook(ActionEvent actionEvent) {
        returnBookButton.setDisable(true);
        returnResultLabel.setTextFill(Color.RED);
        if (returnBookIdTextField.getText().isEmpty()) {
            returnResultLabel.setText("请输入待还图书书号");
        }
        else {
            try {
                int ret = borrowService.returnBook(Integer.parseInt(returnBookIdTextField.getText()));
                switch (ret) {
                    case BorrowService.RECORD_NOT_FOUND:
                        returnResultLabel.setText("无该书借阅记录");
                        break;
                    case BorrowService.BORROW_PERIOD_EXCEED:
                        returnResultLabel.setText(String.format("超出借书期限，已产生罚款 %.2f 元", borrowService.getFine()));
                        break;
                    default:
                        returnResultLabel.setTextFill(Color.GREEN);
                        returnResultLabel.setText("还书成功");
                        break;
                }
            } catch (SQLException e) {
                returnResultLabel.setText(e.getLocalizedMessage());
            }
        }
        returnBookButton.setDisable(false);
    }

    public void payFine(ActionEvent actionEvent) {
        payFineButton.setDisable(true);
        finePaymentResultLabel.setTextFill(Color.RED);
        if (finePaymentStudentIdTextField.getText().isEmpty()) {
            finePaymentResultLabel.setText("请输入借书证号");
        }
        else {
            try {
                int ret = borrowService.payFine(Integer.parseInt(finePaymentStudentIdTextField.getText()));
                if (ret == BorrowService.RECORD_NOT_FOUND) {
                    finePaymentResultLabel.setText("找不到该借书证");
                }
                else {
                    fineLabel.setText(String.format("%.2f 元", borrowService.getFine(Integer.parseInt(finePaymentStudentIdTextField.getText()))));
                    finePaymentResultLabel.setTextFill(Color.GREEN);
                    finePaymentResultLabel.setText("缴费成功");
                }
            } catch (SQLException e) {
                finePaymentResultLabel.setText(e.getLocalizedMessage());
            }
        }
        payFineButton.setDisable(false);
    }
}
