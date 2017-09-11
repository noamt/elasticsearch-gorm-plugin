package test.boost

class Blog {

    String title
    String content

    String tags

    static searchable = {
        title boost: 2.0
    }
}
