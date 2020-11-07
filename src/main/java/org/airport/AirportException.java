package org.airport;

/**
 * Внутреннее исключение для пакета
 */

public class AirportException extends RuntimeException {
    /**
     * Стандартный конструктор исключения
     */
    public AirportException() {
        super("Ошибка: Исключение в системе Аэропорт");
    }

    /**
     * Конструктор исключения с сообщением
     * @param msg Сообщение об ошибке
     */
    public AirportException(String msg) {
        super(msg);
    }
}