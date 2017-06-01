package grails.plugins.elasticsearch

import grails.test.mixin.integration.Integration
import grails.transaction.Rollback
import org.elasticsearch.index.query.QueryBuilders
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification
import test.all.Post

/**
 * @author <a href='mailto:donbeave@gmail.com'>Alexey Zhokhov</a>
 */
@Integration
@Rollback
class AnalyzersIntegrationSpec extends Specification {

    @Autowired
    ElasticSearchAdminService elasticSearchAdminService
    @Autowired
    ElasticSearchService elasticSearchService

    def setup() {
        new Post(subject: "[abc] Grails 3.0 M1 Released!",
                body: "Grails 3.0 milestone 1 is now available.").save(failOnError: true)
        new Post(subject: "The Future of Groovy and Grails Sponsorship",
                body: "[abc] http://grails.io/post/108534902333/the-future-of-groovy-grails-sponsorship").save(failOnError: true)
        new Post(subject: "GORM for MongoDB 3.0 Released",
                body: "[xyz] GORM for MongoDB 3.0 has been released with support for MongoDB 2.6 features, including the " +
                        "new GeoJSON types and full text search.").save(failOnError: true)
    }

    def cleanup() {
        Post.all.each { Post post ->
            post.delete()
        }
    }

    def "search by all"() {
        given:
        elasticSearchAdminService.refresh()

        expect:
        elasticSearchService.search('xyz', [indices: Post, types: Post]).total == 3

        when:
        def results = Post.search('xyz')

        then:
        results.total == 3
    }

    def "search by subject"() {
        given:
        elasticSearchAdminService.refresh()

        expect:
        elasticSearchService.search([indices: Post, types: Post], QueryBuilders.matchQuery('subject', 'xyz')).total == 0

        when:
        def results = Post.search {
            match(subject: 'xyz')
        }

        then:
        results.total == 0
    }

    def "search by body"() {
        given:
        elasticSearchAdminService.refresh()
        expect:
        elasticSearchService.search([indices: Post, types: Post], QueryBuilders.matchQuery('body', 'xyz')).total == 1

        when:
        def results = Post.search {
            match(body: 'xyz')
        }

        then:
        results.total == 1
        results.searchResults[0].body.startsWith('[xyz] GORM')
    }

}
