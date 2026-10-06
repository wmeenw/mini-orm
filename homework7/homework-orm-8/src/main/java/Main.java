import model.Student;
import orm.repository.DBConfig;
import orm.repository.EntityManager;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class Main {
    public static void main(String[] args) {
        try (Connection connection = DBConfig.getConnection()) {
            EntityManager entityManager = new EntityManager(connection);
            entityManager.createTable(Student.class);

            Student st1 = new Student("Ivan", 18, 11, LocalDate.of(2020, 10, 11));
            entityManager.save(st1);

            Student st2 = new Student("Eve", 7, 1, LocalDate.of(2021, 10, 11));
            entityManager.save(st2);

            Student st3 = new Student("Kate", 15, 7, LocalDate.of(2022, 10, 11));
            entityManager.save(st3);

            Optional<Student> found = entityManager.findById(Student.class, 1L);
            found.ifPresent(s -> System.out.println("Найден: " + s.getName()));

            List<Student> all = entityManager.findAll(Student.class);
            System.out.println("Всего студентов: " + all.size());

            Optional<Student> notFound = entityManager.findById(Student.class, 999L);
            System.out.println("Найден по id 999: " + notFound.isPresent());

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
