package model;

import orm.annotation.Column;
import orm.annotation.Id;
import orm.annotation.Table;

@Table(name = "students")
public class Student {
    @Id
    private Long id;
    @Column
    private String name;
    @Column
    private int age;
    @Column
    private double grade;

    public Student(){}

    public Student(String name, int age, double grade) {
        this.name = name;
        this.age = age;
        this.grade = grade;
    }

    public Long getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public int getAge() {
        return age;
    }
    public double getGrade() {
        return grade;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setAge(int age) {
        this.age = age;
    }
    public void setGrade(double grade) {
        this.grade = grade;
    }
}
