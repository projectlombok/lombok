package test;

class Person {
    private final String name;
    private final int age;

    public Person(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public String getName() { return name; }
    public int getAge() { return age; }

    // generated copyWith method
    public Person copyWith(String name, int age) {
        return new Person(
                name != null ? name : this.name,
                age != 0 ? age : this.age
        );
    }
}
ی