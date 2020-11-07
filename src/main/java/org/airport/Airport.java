package org.airport;

import java.io.*;
import java.util.*;

/**
 * Класс, реализующий основную функциональность системы
 */
public class Airport {
    private Map<String, Plane> planes;  //зарегистрированные в аэропорту самолеты
    private List<String> circlingQ;     //очередь самолетов на посадку
    private Runway[] runways;           //посадочные полосы аэропорта

    /**
     * Конструктор класса Аэропорт через загрузку файла
     * @param filenameIn Имя файла
     * @throws IOException Ошибка при чтении файла
     * @throws ClassNotFoundException Ошибка при разборе файла
     */
    public Airport (String filenameIn) throws IOException, ClassNotFoundException {
        load(filenameIn);
    }

    /**
     * Конструктор пустого класса Аэропорт
     * @param numIn Число посадочных полос
     * @throws AirportException Ошибка при создании базового класса логики
     */
    public Airport(int numIn) throws AirportException {
        try {
            runways = new Runway[numIn];
            for (int i = 0; i < numIn; i++) {
                runways[i] = new Runway(i + 1);
            }
            planes = new HashMap<>();
            circlingQ = new ArrayList<>();
        }
        catch (Exception e) {
            throw new AirportException("Ошибка при создании аэропорта, приведшая к закрытию приложения");
        }
    }

    /**
     * Регистрация рейса
     * @param flightIn Номер рейса
     * @param cityOfOrigin Аэропорт отправления
     * @throws AirportException Повторная регистрация
     */
    public void registerFlight(String flightIn, String cityOfOrigin) throws AirportException{
        if (planes.containsKey(flightIn))
            throw new AirportException("Рейс " + flightIn + " уже был зарегистрирован");
        Plane plane = new Plane(flightIn, cityOfOrigin);
        planes.put(flightIn, plane);
    }

    /**
     * Записывает информацию о прибытии самолета
     * @param flightIn Номер рейса
     * @return Номер посадочной полосы
     * @throws AirportException Ошибка при посадке или включении в очередь
     */
    public int arriveAtAirport(String flightIn) throws AirportException {
        Runway vacantRunway = nextFreeRunway();
        if (vacantRunway != null) {
            descend(flightIn, vacantRunway);
            return vacantRunway.getNumber();
        }
        else {
            circle(flightIn);
            return 0;
        }
    }

    /**
     * Записывает информацию о посадке
     * @param flightIn Номер рейса
     * @param runwayNumberIn Номер посадочной полосы
     * @throws AirportException Ошибка в статусе рейса
     */
    public void landAtAirport(String flightIn, int runwayNumberIn) throws AirportException {
        Plane plane = getPlane(flightIn);
        if (plane.getRunwayNumber() != runwayNumberIn)
            throw new AirportException("Самолет с рейса " + flightIn + " не должен садиться на эту полосу!");
        if (plane.getStatus() == PlaneStatus.DUE)
            throw new AirportException("Самолет с рейса " + flightIn + " не сообщал о прибытии!");
        if (plane.getStatus().compareTo(PlaneStatus.WAITING) > 0)
            throw new AirportException("Самолет с рейса " + flightIn + " уже приземлился!");
        plane.upgradeStatus();
    }

    /**
     * Записывает информацию о готовности к посадке пассажиров
     * @param flightIn Номер рейса
     * @param destination Аэропорт отправления
     * @throws AirportException Ошибка в статусе рейса
     */
    public void readyForBoarding(String flightIn, String destination) throws AirportException {
        Plane plane = getPlane(flightIn);
        if (plane.getStatus().compareTo(PlaneStatus.LANDED) < 0)
            throw new AirportException("Самолет с рейса " + flightIn + " не приземлялся!");
        if (plane.getStatus() == PlaneStatus.DEPARTING)
            throw new AirportException("Самолет с рейса " + flightIn + " уже ожидает вылет!");
        plane.upgradeStatus();
        plane.changeCity(destination);
    }

    /**
     * Записывает информацию о взлете
     * @param flightIn Номер рейса
     * @return Экземпляр нового обрабатываемого самолета
     * @throws AirportException Ошибка при включении в очередь
     */
    public Plane takeOff(String flightIn) throws AirportException {
        leave(flightIn);
        Plane nextFlight = nextToLand();
        if (nextFlight != null) {
            Runway vacantRunway = nextFreeRunway();
            descend(nextFlight.getFlightNumber(), vacantRunway);
            return nextFlight;
        }
        return null;
    }

    /**
     * Лист ожидания прибывающих самолетов
     * @return Лист ожидания в форме множества
     */
    public Set<Plane> getArrivals() {
        Set<Plane> planesOut = new HashSet<>();
        Set<String> items = planes.keySet();
        for (String thisFlight : items) {
            Plane plane = planes.get(thisFlight);
            if (plane.getStatus() != PlaneStatus.DEPARTING)
                planesOut.add(plane);
        }
        return planesOut;
    }

    /**
     * Лист ожидания улетающих самолетов
     * @return Лист ожидания в форме множества
     */
    public Set<Plane> getDepartures() {
        Set<Plane> planesOut = new HashSet<>();
        Set<String> items = planes.keySet();
        for (String thisFlight : items) {
            Plane plane = planes.get(thisFlight);
            if (plane.getStatus() == PlaneStatus.DEPARTING)
                planesOut.add(plane);
        }
        return planesOut;
    }

    /**
     * Возвращает число посадочных полос
     * @return Общее число посадочных полос
     */
    public int getNumberOfRunways() {
        return runways.length;
    }

    /**
     * Выгружает текущее состояние в файл
     * @param fileIn Имя файла
     * @throws IOException Ошибка при записи файла
     */
    public void save(String fileIn) throws IOException {
        try  (FileOutputStream fileOut = new FileOutputStream(fileIn);
              ObjectOutputStream objOut = new ObjectOutputStream(fileOut)){
            objOut.writeObject(planes);
            objOut.writeObject(circlingQ);
            objOut.writeObject(runways);
        }
    }

    /**
     * Загружает текущее состояние из файла
     * @param fileName Имя файла
     * @throws IOException Ошибка при чтении файла
     * @throws ClassNotFoundException Ошибка при разборе файла
     */
    public void load(String fileName) throws IOException, ClassNotFoundException {
        try  (FileInputStream fileInput = new FileInputStream(fileName);
              ObjectInputStream objInput = new ObjectInputStream(fileInput)){
            planes = (Map<String, Plane>)objInput.readObject();
            circlingQ = (List<String>)objInput.readObject();
            runways = (Runway[])objInput.readObject();
        }
    }

    /**
     * Поиск свободной полосы
     * @return Свободная посадочная полоса
     */
    private Runway nextFreeRunway() {
        for (Runway nextRunway : runways) {
            if (!nextRunway.isAllocated())
                return nextRunway;
        }
        return null;
    }

    /**
     * Поиск самолета по номеру рейса
     * @param flightIn Номер рейса
     * @return Экземпляр класса Самолет
     * @throws AirportException Ошибка при получении номера рейса
     */
    private Plane getPlane(String flightIn) throws AirportException {
        if (!planes.containsKey(flightIn))
            throw new AirportException("Рейс " + flightIn + " не был зарегистрирован");
        return planes.get(flightIn);
    }

    /**
     * Выполняет действия при готовности к посадке
     * @param flightIn Номер рейса
     * @param runwayIn Посадочная полоса
     * @throws AirportException Ошибка статуса рейса
     */
    private void descend(String flightIn, Runway runwayIn) throws AirportException {
        Plane plane = getPlane(flightIn);
        if (plane.getStatus().compareTo(PlaneStatus.WAITING) > 0)
            throw new AirportException("Самолет с рейса " + flightIn
                    + " уже в аэропорту со статусом " + plane.getStatusName());
        if (plane.isAllocatedRunway())
            throw new AirportException("Самолет с рейса " + flightIn
                    + " уже приписан к полосе " + plane.getRunwayNumber());
        plane.allocateRunway(runwayIn);
        if (plane.getStatus() == PlaneStatus.DUE)
            plane.upgradeStatus();
    }

    /**
     * Выполняет действия при неготовности аэропорта к посадке
     * @param flightIn Номер рейса
     * @throws AirportException Ошибка статуса рейса
     */
    private void circle(String flightIn) throws AirportException {
        Plane plane = getPlane(flightIn);
        if (plane.getStatus() != PlaneStatus.DUE)
            throw new AirportException("Самолет с рейса " + flightIn + " уже прибыл в аэропорт");
        plane.upgradeStatus();
        circlingQ.add(flightIn);
    }

    /**
     * Выполняет действия при взлете самолета
     * @param flightIn Номер рейса
     * @throws AirportException Ошибка статуса рейса
     */
    private void leave(String flightIn) throws AirportException {
        Plane plane = getPlane(flightIn);
        if (plane.getStatus().compareTo(PlaneStatus.LANDED) < 0)
            throw new AirportException("Самолет с рейса " + flightIn + " не приземлялся!");
        if (plane.getStatus() == PlaneStatus.LANDED)
            throw new AirportException("На самолет с рейса " + flightIn + " не была объявлена посадка!");
        plane.vacateRunway();
        planes.remove(flightIn);
    }

    /**
     * Возвращает самолет который можно посадить
     * @return Следующий самолет готовый к посадке
     */
    private Plane nextToLand() {
        if (!circlingQ.isEmpty()) {
            String flight = circlingQ.get(0);
            circlingQ.remove(flight);
            return getPlane(flight);
        }
        return null;
    }
}