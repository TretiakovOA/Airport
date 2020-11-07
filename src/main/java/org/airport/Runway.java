package org.airport;

import java.io.Serializable;

/**
 * Посадочная полоса
 */
public class Runway implements Serializable {
    private int number;         //Номер полосы
    private boolean allocated;  //Назначен ли самолет на эту полосу

    /**
     * Конструктор посадочной полосы
     * @param numberIn Номер посадочной полосы
     */
    public Runway(int numberIn) {
        if (numberIn < 1)
            throw new AirportException("Неверный номер посадочной полосы " + numberIn);
        number = numberIn;
        allocated = false;
    }

    /**
     * Геттер номера полосы
     * @return Номер посадочной полосы
     */
    public int getNumber() {
        return number;
    }

    /**
     * Геттер назначения самолета
     * @return Булево, назначен ли самолет на данную полосу
     */
    public boolean isAllocated() {
        return allocated;
    }

    /**
     * Бронирование полосы для самолета
     */
    public void book() {
        allocated = true;
    }

    /**
     * Освобождение посадочной полосы
     */
    public void vacate() {
        allocated = false;
    }
}