package org.airport;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import java.util.Set;

public class AirportFrame extends Application {
    private Airport airport;                        //Основной класс приложения
    private int numberOfRunways;                    //Число посадочных полос
    private final String FILENAME = "airport.dat";  //Файл для хранения текущего состояния

    private HBox arrivals = new HBox(50);        //Список прибытий
    private VBox arrivalsColumn1 = new VBox();      //Колонка "РЕЙС"
    private VBox arrivalsColumn2 = new VBox();      //Колонка "ОТКУДА"
    private VBox arrivalsColumn3 = new VBox();      //Колонка "СТАТУС"
    private VBox arrivalsColumn4 = new VBox();      //Колонка "ПОЛОСА"

    private HBox departures = new HBox(60);      //Список отправлений
    private VBox departuresColumn1 = new VBox();    //Колонка "РЕЙС"
    private VBox departuresColumn2 = new VBox();    //Колонка "КУДА"
    private VBox departuresColumn3 = new VBox();    //Колонка "ПОЛОСА"

    /**
     * Метод инициализации приложения
     * @param primaryStage Начальный экран
     */
    @Override
    public void start(Stage primaryStage) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION,
                "Восстановить предыдущее состояние?", ButtonType.YES, ButtonType.NO);
        alert.setTitle("Требуется подтверждение");
        alert.setHeaderText("Запрос к пользователю");
        String response = alert.showAndWait().get().getText();
        if (response.equals("Yes")) {
            try {
                airport = new Airport(FILENAME);
                listArrivals();
                listDepartures();
                showInfo("Информация загружена");
            } catch (Exception e) {
                showError("Ошибка при открытии файла");
                System.exit(1);     //Выход из программы с ошибкой
            }
        } else {
            numberOfRunways = getNumberOfRunways();
            try {
                airport = new Airport(numberOfRunways);
            } catch (AirportException ae) {
                showError(ae.getMessage());
                System.exit(1);     //Выход из программы с ошибкой
            } catch (Exception e) {
                showError(e.getMessage());
                System.exit(1);     //Выход из программы с ошибкой
            }
        }

        VBox root = initGUI();
        primaryStage.setTitle("АЭРОПОРТ");
        primaryStage.setScene(new Scene(root, 640, 480));
        primaryStage.initStyle(StageStyle.UNDECORATED);
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Создание графического интерфейса приложения
     * @return Контейнер с элементами графического интерфейса приложения
     */
    private VBox initGUI() {
        VBox root = new VBox();
        TabPane tabPane = new TabPane();
        Tab tab1 = new Tab("Управление полетами");
        Tab tab2 = new Tab("Прибытия");
        Tab tab3 = new Tab("Отправления");
        tabPane.getTabs().addAll(tab1, tab2, tab3);

        MenuBar bar = new MenuBar();
        bar.setMinHeight(25);
        Menu item = new Menu("Файл");
        Menu saveAndContinueOption = new Menu("Сохранить и продолжить");
        Menu saveAndExitOption = new Menu("Сохранить и выйти");
        Menu exitWithoutSavingOption = new Menu("Выйти без сохранения");
        item.getItems().addAll(saveAndContinueOption, saveAndExitOption, exitWithoutSavingOption);
        bar.getMenus().add(item);
        try {
            saveAndContinueOption.setOnAction(e -> save(FILENAME));
            saveAndExitOption.setOnAction(e -> {
                save(FILENAME);
                Platform.exit();
            });
            exitWithoutSavingOption.setOnAction(e -> exitWithoutSaving());
        } catch (Exception e) {
            showError("Некорректная операция");
        }

        tab1.setContent(initManageBox());
        arrivals.setPadding(new Insets(10));
        arrivals.getChildren().addAll(arrivalsColumn1, arrivalsColumn2, arrivalsColumn3, arrivalsColumn4);
        tab2.setContent(arrivals);
        departures.setPadding(new Insets(10));
        departures.getChildren().addAll(departuresColumn1, departuresColumn2, departuresColumn3);
        tab3.setContent(departures);

        root.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID,
                new CornerRadii(0), new BorderWidths(2))));
        root.getChildren().addAll(bar, tabPane);
        return root;
    }

    /**
     * Создание вкладки с управлением
     * @return Вкладка с управлением системой
     */
    private VBox initManageBox() {
        VBox box = new VBox();
        box.setMinHeight(400);
        box.setAlignment(Pos.BOTTOM_LEFT);
        Image image = new Image("clouds.jpg");
        ImageView imageView = new ImageView(image);
        Label label = new Label("Выберите действие:");
        HBox controls = new HBox(10);
        Button button1 = new Button("Регистрация");
        button1.setTooltip(new Tooltip("Регистрация рейса в аэропорту"));
        Button button2 = new Button("Запрос на посадку");
        button2.setTooltip(new Tooltip("Запросить посадку в аэропорту"));
        Button button3 = new Button("Приземление");
        button3.setTooltip(new Tooltip("Ввести информацию о приземлении в аэропорту"));
        Button button4 = new Button("Начало посадки");
        button4.setTooltip(new Tooltip("Ввести информацию о начале посадки в аэропорту"));
        Button button5 = new Button("Взлет");
        button5.setTooltip(new Tooltip("Ввести информацию о взлете в аэропорту"));
        controls.getChildren().addAll(button1, button2, button3, button4, button5);
        box.getChildren().addAll(imageView, label, controls);

        try {
            button1.setOnAction(e -> register());
            button2.setOnAction(e -> requestToLand());
            button3.setOnAction(e -> land());
            button4.setOnAction(e -> board());
            button5.setOnAction(e -> takeOff());
        } catch (Exception e) {
            showError("Некорректная операция");
        }
        return box;
    }

    /**
     * Возвращает число посадочных полос в аэропорту
     * @return Количество посадочных полос
     */
    private int getNumberOfRunways() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setHeaderText("Введите количество посадочных полос");
        dialog.setTitle("Запрос информации по посадочным полосам");
        String response = dialog.showAndWait().get();
        if (!response.equals(""))
            return Integer.parseInt(response);
        return -1;  //не указано количество посадочных полос
    }

    /**
     * Регистрация нового рейса в аэропорту
     */
    private void register() {
        String flightNo, city;
        try {
            flightNo = getFlightNo("Форма для посадки");
            TextInputDialog dialog = new TextInputDialog();
            dialog.setHeaderText("Введите аэропорт отправления");
            dialog.setTitle("Форма регистрации");
            city = dialog.showAndWait().get();
            checkIfEmpty(city, "Не указан аэропорт отправления");

            airport.registerFlight(flightNo, city);
            showInfo("Подтвержден рейс " + flightNo + " из аэропорта " + city);
        } catch (AirportException ae) {
            showError(ae.getMessage());
        }
        listArrivals();
    }

    /**
     * Запрос на посадку
     */
    private void requestToLand() {
        String flightNo, message;
        try {
            flightNo = getFlightNo("Запрос на посадку");
            int runway = airport.arriveAtAirport(flightNo);
            message = runway == 0 ? "Нет свободной полосы, ждите освобождения" : "Ваша полоса: " + runway;
            showInfo("Сообщение для рейса " + flightNo + ": " + message);
        } catch (AirportException ae) {
            showError(ae.getMessage());
        }
        listArrivals();
    }

    /**
     * Посадка
     */
    private void land() {
        String flightNo, runwayIn;
        int runway;
        try {
            flightNo = getFlightNo("Форма посадки");
            TextInputDialog dialog = new TextInputDialog();
            dialog.setHeaderText("Введите номер посадочной полосы");
            dialog.setTitle("Форма посадки");
            runwayIn = dialog.showAndWait().get();
            checkIfEmpty(runwayIn, "Не указана посадочная полоса");
            runway = Integer.parseInt(runwayIn);

            airport.landAtAirport(flightNo, runway);
            showInfo("Рейс " + flightNo + " осуществил посадку на полосе " + runway);
        } catch (AirportException ae) {
            showError(ae.getMessage());
        }
        listArrivals();
    }

    /**
     * Посадка на рейс
     */
    private void board() {
        String flightNo, city;
        try {
            flightNo = getFlightNo("Объявлена посадка на рейс");
            TextInputDialog dialog = new TextInputDialog();
            dialog.setHeaderText("Введите аэропорт назначения");
            dialog.setTitle("Объявлена посадка на рейс");
            city = dialog.showAndWait().get();
            checkIfEmpty(city, "Не указан аэропорт назначения");

            airport.readyForBoarding(flightNo, city);
            showInfo("На рейс " + flightNo + " объявлена посадка. Аэропорт назначения: " + city);
        } catch (AirportException ae) {
            showError(ae.getMessage());
        }
        listArrivals();
        listDepartures();
    }

    /**
     * Взлет
     */
    private void takeOff() {
        String flightNo;
        try {
            flightNo = getFlightNo("Осуществлен взлет");
            airport.takeOff(flightNo);
            showInfo("Рейс " + flightNo + " исключен из системы");
        } catch (AirportException ae) {
            showError(ae.getMessage());
        }
        listDepartures();
    }

    /**
     * Получение номера рейса от пользователя
     * @param title Текст заголовка
     * @return Номер рейса
     * @throws AirportException Ошибка при получении номера рейса
     */
    private String getFlightNo(String title) throws AirportException {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setHeaderText("Введите номер рейса");
        dialog.setTitle(title);
        String flightNo = dialog.showAndWait().get();
        checkIfEmpty(flightNo, "Не указан номер рейса");
        return flightNo;
    }

    /**
     * Список прибытия
     */
    private void listArrivals() {
        Set<Plane> arrivalsList = airport.getArrivals();
        arrivalsColumn1.getChildren().clear();
        arrivalsColumn2.getChildren().clear();
        arrivalsColumn3.getChildren().clear();
        arrivalsColumn4.getChildren().clear();
        arrivalsColumn1.getChildren().add(new Text("РЕЙС"));
        arrivalsColumn2.getChildren().add(new Text("ОТКУДА"));
        arrivalsColumn3.getChildren().add(new Text("СТАТУС"));
        arrivalsColumn4.getChildren().add(new Text("ПОЛОСА"));
        for (Plane plane : arrivalsList) {
            arrivalsColumn1.getChildren().add(new Text(plane.getFlightNumber()));
            arrivalsColumn2.getChildren().add(new Text(plane.getCity()));
            arrivalsColumn3.getChildren().add(new Text(plane.getStatusName()));
            try {
                arrivalsColumn4.getChildren().add(new Text(Integer.toString(plane.getRunwayNumber())));
            } catch (Exception e) {
                arrivalsColumn4.getChildren().add(new Text(""));
            }
        }
    }

    /**
     * Список отправления
     */
    private void  listDepartures() {
        Set<Plane> departuresList = airport.getDepartures();
        departuresColumn1.getChildren().clear();
        departuresColumn2.getChildren().clear();
        departuresColumn3.getChildren().clear();
        departuresColumn1.getChildren().add(new Text("РЕЙС"));
        departuresColumn2.getChildren().add(new Text("КУДА"));
        departuresColumn3.getChildren().add(new Text("ПОЛОСА"));

        for (Plane plane : departuresList) {
            departuresColumn1.getChildren().add(new Text(plane.getFlightNumber()));
            departuresColumn2.getChildren().add(new Text(plane.getCity()));
            try {
                departuresColumn3.getChildren().add(new Text(Integer.toString(plane.getRunwayNumber())));
            } catch (Exception e) {
                departuresColumn3.getChildren().add(new Text(""));
            }
        }
    }

    /**
     * Выход без сохранения
     */
    private void exitWithoutSaving() {
        Alert alert = new Alert(Alert.AlertType.WARNING, "Вы уверены? Все данные будут потеряны",
                ButtonType.YES, ButtonType.CANCEL);
        alert.setTitle("Требуется подтверждение");
        alert.setHeaderText("Предупреждение");
        String response = alert.showAndWait().get().getText();
        if (response.equals("Yes"))
            Platform.exit();
    }

    /**
     * Загрузка данных
     * @param fileName Имя файла
     */
    private void open(String fileName) {
        try {
            airport.load(fileName);
            listArrivals();
            listDepartures();
            showInfo("Данные загружены");
        } catch (Exception e) {
            showError("Ошибка при открытии файла");
            System.exit(1);     //Выход из программы с ошибкой
        }
    }

    /**
     * Сохранение данных
     * @param fileName Имя файла
     */
    private void save(String fileName) {
        try {
            airport.save(fileName);
            showInfo("Данные сохранены");
        } catch (Exception e) {
            showError("Ошибка при сохранении файла");
        }
    }

    /**
     * Отображение ошибки
     * @param msg Сообщение
     */
    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText("Ошибка в системе");
        alert.setContentText(msg);
        alert.showAndWait();
    }

    /**
     * Отображение информации
     * @param msg Сообщение
     */
    private void showInfo(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText("Информация о системе");
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void checkIfEmpty(String s, String errorMsg) {
        if (s.equals(""))
            throw new AirportException(errorMsg);
    }
}