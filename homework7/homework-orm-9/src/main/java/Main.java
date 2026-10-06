import model.Book;
import orm.repository.DBConfig;
import orm.repository.EntityManager;
import orm.core.OrmException;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class Main {
    public static void main(String[] args) {
        try (Connection connection = DBConfig.getConnection()) {
            EntityManager em = new EntityManager(connection);
            em.createTable(Book.class);

            List<Book> books = List.of(
                    new Book("Война и мир", "Толстой", 1869, true),
                    new Book("Анна Каренина", "Толстой", 1877, true),
                    new Book("Преступление и наказание", "Достоевский", 1866, true),
                    new Book("Идиот", "Достоевский", 1869, false),
                    new Book("Мастер и Маргарита", "Булгаков", 1967, true)
            );
            em.saveAll(books);
            System.out.println("Сохранено книг: " + books.size());

            List<Book> tolstoy = em.findAllWhere(Book.class, "author", "Толстой");
            System.out.println("Книги Толстого: " + tolstoy.size());

            Book first = tolstoy.get(0);
            first.setAvailable(false);
            em.update(first);
            System.out.println("Книга обновлена");

            Optional<Book> one = em.findOneWhere(Book.class, "title", "Идиот");
            one.ifPresent(b -> System.out.println("Найдена: " + b.getTitle()));

            em.delete(first);
            System.out.println("Книг после удаления: " + em.count(Book.class));

            try {
                em.saveAll(List.of(
                        new Book("Нормальная книга", "Автор", 2000, true),
                        new Book(null, "Автор", 2001, true)
                ));
            } catch (OrmException e) {
                System.out.println("Откат saveAll: " + e.getMessage());
            }

            System.out.println("Книг после отката: " + em.count(Book.class));

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}