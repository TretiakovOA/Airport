package org.airport;

/**
 * Перечисление для определения статуса самолета
 */
public enum PlaneStatus {
    DUE,        //назначен в аэропорт
    WAITING,    //ожидает посадки
    LANDED,     //приземлился
    DEPARTING   //ожидает вылета
}