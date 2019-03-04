package com.github.sftwnd.crayfish.common.exception;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.Callable;
import java.util.function.Function;

/**
 * Утилиты для работы с исключениями
 */
public final class ExceptionUtils {

    private ExceptionUtils() {
        //NOOP
    }

    /**
     * Используется как обёртка вокруг checked-исключения
     */
    @SuppressWarnings("serial")
    public static class WrappedCheckedException extends RuntimeException {
        public WrappedCheckedException(String s, Throwable throwable) {
            super(s, throwable);
        }

        public WrappedCheckedException(Throwable t) {
            super(t);
        }
    }

    public static String getMessage(Throwable e) {
        String message = e.getLocalizedMessage();
        if (message == null || "".equals(message)) {
            return e.getClass().getName();
        } else {
            return message;
        }
    }

    @SuppressWarnings({
            // Callable.call() throws Exception
            "squid:S2221"
    })
    public static <T> T wrapUncheckedExceptions(Callable<T> callable) {
        try {
            return callable.call();
        } catch (Exception t) {
            throw propagate(t);
        }
    }

    @SuppressWarnings({
            // Runnable.run() throws Exception
            "squid:S2221"
    })
    public static void wrapUncheckedExceptions(Process<? extends Exception> process) {
        try {
            process.run();
        } catch (Exception e) {
            throw propagate(e);
        }
    }

    // sneakyThrow
    @SuppressWarnings("unchecked")
    public static final <T extends Throwable, R> R uncheckExceptions(Throwable throwable) throws T {
        throw (T) throwable;
    }

    /**
     * Convenience method to throw a {@link RuntimeException} and {@link Error} directly
     * or wrap any other exception type into a arbitrary descendant of {@link RuntimeException} .
     * @param t The exception to throw directly or wrapped.
     * @param wrapper Function that take original exception as a parameter and construct a new exception to throw.
     *                This function is called for checked exceptions (not descendants of {@link RuntimeException} or {@link Error}) only.
     * @return Because {@code propagate} itself throws an exception or error, this is a sort of phantom return
     *         value; {@code propagate} does not actually return anything.
     */
    @SuppressWarnings({
            "squid:CommentedOutCodeLine" // Закоментированный код отсутствует: это действтительно комментарий
    })
    public static RuntimeException propagate(Throwable t, Function<Throwable, ? extends RuntimeException> wrapper) {
        /*
         * The return type of RuntimeException is a trick for code to be like this:
         *
         * throw Exceptions.propagate(e);
         *
         * Even though nothing will return and throw via that 'throw', it allows the code to look like it
         * so it's easy to read and understand that it will always result in a throw.
         */

        if (t instanceof RuntimeException) {
            throw (RuntimeException) t;
        }
        else if (t instanceof Error) {
            throw (Error) t;
        } else {
            throw wrapper.apply(t);
        }
    }

    /**
     * Convenience method to throw a {@link RuntimeException} and {@link Error} directly
     * or wrap any other exception type into a {@link RuntimeException}.
     * @param t the exception to throw directly or wrapped
     * @return because {@code propagate} itself throws an exception or error, this is a sort of phantom return
     *         value; {@code propagate} does not actually return anything
     */
    @SuppressWarnings({
            "squid:CommentedOutCodeLine" // Закоментированный код отсутствует: это действтительно комментарий
    })
    public static RuntimeException propagate(Throwable t) {
        /*
         * The return type of RuntimeException is a trick for code to be like this:
         *
         * throw Exceptions.propagate(e);
         *
         * Even though nothing will return and throw via that 'throw', it allows the code to look like it
         * so it's easy to read and understand that it will always result in a throw.
         */

        throw propagate(t, WrappedCheckedException::new);
    }

    /** Возвращает строку описывающую весть стэк ошибки */
    @SuppressWarnings({
            // Sonar лажается
            "squid:S1148"
    })
    public static String getErrorStackText(Throwable t) {
        StringWriter writer = new StringWriter();
        PrintWriter pwriter = new PrintWriter(writer);
        t.printStackTrace(pwriter);
        pwriter.flush();
        writer.flush();

        return writer.toString();
    }

    /** Возвращает первичное исключение */
    public static Throwable getRootCause(Throwable t) {
        if (t.getCause() == null) {
            return t;
        } else {
            return getRootCause(t.getCause());
        }
    }

    /**
     * Проверяет есть ли в стеке ошибки t указанное исключение класса pattern.
     */
    public static <E extends Throwable> boolean isInCauseStack(Throwable t, Class<E> pattern) {
        return findFirstExcepionOfTheTypeInCauseStack(t, pattern) != null;
    }

    /**
     * Возвращает первое исключение указанного типа из списка причин заднного исключения. Само исключение верхненго
     * уровня (то что передаётся в параметре <code>t</code>) так-же учитывается при поиске и может быть возвращено
     * в качестве результата.
     * @param t Исключение в стеке которого надо искать.
     * @param pattern Тип исключения, которое необходимо найти в стеке.
     * @return Возвращает найденное исключение или <code>null</code> если подходящее исключение не найдено.
     */

    @SuppressWarnings("unchecked")
    public static <E extends Throwable> E findFirstExcepionOfTheTypeInCauseStack(Throwable t, Class<E> pattern) {
        Throwable x = t;
        while (x != null) {
            if (pattern.isInstance(x)) {
                return (E) x;
            }
            x = x.getCause();
        }
        return null;
    }

    /**
     * Оборачивает checked исключение {@link InterruptedException} в unchecked исключение {@link RuntimeException}
     * и выкидывает это новое исключение. Перед выкидыванем исключения вызывает {@link Thread ::currentThread::interrupt}
     * что-бы прокинуть прерывание треда дальше.
     * @param orig Оригинальное исключение, которое необходимо обернуть.
     * @return аналогично описанию для {@link #propagate(Throwable)}.
     */
    public static RuntimeException reinterrupt(InterruptedException orig) {
        return reinterrupt(orig, WrappedCheckedException::new);
    }

    /**
     * Оборачивает checked исключение {@link InterruptedException} в unchecked исключение {@link RuntimeException}
     * и выкидывает это новое исключение. Перед выкидыванем исключения вызывает {@link Thread ::currentThread::interrupt}
     * что-бы прокинуть прерывание треда дальше.
     * @param msg Сообщение об ошибке для выбрасываемого исключения.
     * @param orig Оригинальное исключение, которое необходимо обернуть.
     * @return аналогично описанию для {@link #propagate(Throwable)}.
     */
    public static RuntimeException reinterrupt(String msg, InterruptedException orig) {
        return reinterrupt(orig, e -> new WrappedCheckedException(msg, e));
    }

    /**
     * Оборачивает checked исключение {@link InterruptedException} в unchecked исключение наследник {@link RuntimeException}
     * и выкидывает это новое исключение. Перед выкидыванем исключения вызывает {@link Thread ::currentThread::interrupt}
     * что-бы прокинуть прерывание треда дальше.
     * @param orig Оригинальное исключение, которое необходимо обернуть.
     * @param wrapper Функция получения исключения обёртки.
     * @return аналогично описанию для {@link #propagate(Throwable)}.
     */
    public static RuntimeException reinterrupt(InterruptedException orig, Function<InterruptedException, ? extends RuntimeException> wrapper) {
        Thread.currentThread().interrupt();
        return propagate(wrapper.apply(orig));
    }

}
