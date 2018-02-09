package grails.plugins.elasticsearch.mapping

import grails.plugins.elasticsearch.ElasticSearchContextHolder
import grails.test.mixin.integration.Integration
import spock.lang.Specification
import test.Person

/**
 * Created by @marcos-carceles on 20/05/15.
 */
@Integration
class SearchableDomainClassMapperIntegrationSpec extends Specification {

    ElasticSearchContextHolder elasticSearchContextHolder

    def "elasticSearch.defaultExcludedProperties are not mapped"() {
        expect:
        elasticSearchContextHolder.config.defaultExcludedProperties.contains('password')
        Person.searchable instanceof Closure

        when:
        SearchableClassMapping personMapping = elasticSearchContextHolder.getMappingContextByType(Person)

        then:
        !personMapping.propertiesMapping*.grailsProperty*.name.contains("password")

        when:
        SearchableClassMapping incautiousMapping = elasticSearchContextHolder.getMappingContextByType(Person)

        then:
        !incautiousMapping.propertiesMapping*.grailsProperty*.name.containsAll(["firstName", "lastName", "password"])

    }
}
