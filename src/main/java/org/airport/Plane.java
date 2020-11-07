package org.airport;

import java.io.Serializable;
import java.util.Objects;

/**
 * Детали, связанные с самолетом, выполняющим рейс
 */
public class Plane implements Serializable {
    private  String flightNumber;   //номер рейса
    private String city;            //другой аэропорт
    private PlaneStatus status;     //текущий статус рейса
    private Runway theRunway;       //посадочная полоса

    /**
     * Конструктор прибывающего самолета
     * @param flightIn Номер рейса
     * @param cityOfOrigin Аэропорт отправления
     */
    public Plane(String flightIn, String cityOfOrigin) {
        flightNumber = flightIn;
        city = cityOfOrigin;
        status = PlaneStatus.DUE;   //Вылетел из порта отправления
        theRunway = null;           //Полоса не назначена
    }

    /**
     * Геттер номера рейса
     * @return Номер рейса
     */
    public String getFlightNumber() {
        return flightNumber;
    }

    /**
     * Геттер города отправления
     * @return  Город отправления
     */
    public String getCity() {
        return city;
    }

    /**
     * Геттер статуса рейса
     * @return Статус рейса
     */
    public PlaneStatus getStatus() {
        return status;
    }

    /**
     * Геттер посадочной полосы
     * @return Посадочная полоса
     */
    public Runway getTheRunway() {
        return theRunway;
    }

    /**
     * Геттер номера посадочной полосы
     * @return Номер посадочной полосы
     * @throws AirportException Не назначена посадочная полоса
     */
    public int getRunwayNumber() throws AirportException {
        if (theRunway == null)
            throw new AirportException("Рейсу " + flightNumber + " не назначена посадочная полоса");
        return theRunway.getNumber();
    }

    /**
     * Геттер назначения посадочной полосы
     * @return Назначена ли посадочная полоса
     */
    public boolean isAllocatedRunway() {
        return theRunway != null;
    }

    /**
     * Назначение посадочной полосы
     * @param runwayIn Посадочная полоса
     * @throws AirportException Неверная полоса
     */
    public void allocateRunway(Runway runwayIn) throws AirportException {
        if (runwayIn == null)
            throw new AirportException("Передана отсутствующая полоса");
        if (runwayIn.isAllocated())
            throw new AirportException("Полоса забронирована под другой рейс");
        theRunway = runwayIn;
        theRunway.book();
    }

    /**
     * Освобождение посадочной полосы
     * @throws AirportException Не выделена полоса
     */
    public void vacateRunway() throws AirportException {
        if (theRunway == null)
            throw new AirportException("Отсутствует выделенная полоса");
        theRunway.vacate();
    }

    /**
     * Геттер статуса самолета
     * @return Статус самолета
     */
    public String getStatusName() {
        return status.toString();
    }

    /**
     * Изменение статуса самолета
     * @throws AirportException Некорректный вызов метода
     */
    public void upgradeStatus() throws AirportException {
        switch (status) {
            case DUE:       status = PlaneStatus.WAITING;   break;
            case WAITING:   status = PlaneStatus.LANDED;    break;
            case LANDED:    status = PlaneStatus.DEPARTING; break;
            case DEPARTING: throw new AirportException("Нельзя изменить статус DEPARTING");
        }
    }

    /**
     * Изменение связанного с рейсом города
     * @param destination Другой аэропорт в рейсе
     */
    public void changeCity(String destination ) {
        city = destination;
    }

    /**
     * Получение информации о самолете
     * @return Информация о самолете
     */
    @Override
    public String toString() {
        String out = "номер рейса: " + flightNumber + "\tгород: " + city + "\tстатус: " + status;
        if (theRunway != null)
            out = out + "\tполоса: " + theRunway;
        return out;
    }

    /**
     * Сравнение с другим объектом
     * @param objIn Приходящий объект
     * @return Равны ли объекты
     */
    @Override
    public boolean equals(Object objIn) {
        if (objIn != null) {
            Plane plane = (Plane) objIn;
            return flightNumber.equals(plane.flightNumber);
        }
        return false;
    }

    /**
     * Получение хэш-кода объекта
     * @return Хэш-код
     */
    @Override
    public int hashCode() {
        return Objects.hash(flightNumber);
    }
}