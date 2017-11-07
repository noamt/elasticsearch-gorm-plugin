package grails.plugins.elasticsearch

import spock.lang.Specification

import grails.testing.mixin.integration.Integration
import grails.transaction.Rollback

import org.elasticsearch.index.query.QueryBuilders
import test.all.Post

/**
 * @author <a href='mailto:donbeave@gmail.com'>Alexey Zhokhov</a>
 */
@Integration
@Rollback
class AnalyzersIntegrationSpec extends Specification implements ElasticSearchSpec {

    def setup() {
        save new Post(
                subject: "[abc] Grails 3.0 M1 Released!",
                body: "Grails 3.0 milestone 1 is now available.")

        save new Post(
                subject: "The Future of Groovy and Grails Sponsorship",
                body: "[abc] http://grails.io/post/108534902333/the-future-of-groovy-grails-sponsorship")

        save new Post(
                subject: "GORM for MongoDB 3.0 Released",
                body: "[xyz] GORM for MongoDB 3.0 has been released with support for MongoDB 2.6 features, including" +
                        "the new GeoJSON types and full text search.")
    }

    def cleanup() {
        Post.list().each { it.delete() }
    }

    def "search by all"() {
        given:
        refreshIndices()

        expect:
        search(Post, 'xyz').total == 3

        when:
        def results = Post.search('xyz')

        then:
        results.total == 3
    }

    def "search by subject"() {
        given:
        refreshIndices()

        expect:
        search(Post, QueryBuilders.matchQuery('subject', 'xyz')).total == 0

        when:
        def results = Post.search {
            match(subject: 'xyz')
        }

        then:
        results.total == 0
    }

    def "search by body"() {
        given:
        refreshIndices()

        expect:
        search(Post, QueryBuilders.matchQuery('body', 'xyz')).total == 1

        when:
        def results = Post.search {
            match(body: 'xyz')
        }

        then:
        results.total == 1
        results.searchResults[0].body.startsWith('[xyz] GORM')
    }

}
