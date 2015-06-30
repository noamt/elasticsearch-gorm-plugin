package test.moreLikeThis;

public class Question {
	String title
    String description = "A test description of a question"
    
    static searchable = true

    static constraints = {
		title blank: false
        description nullable: true
    }

    static mapping = {
        
    }
}
