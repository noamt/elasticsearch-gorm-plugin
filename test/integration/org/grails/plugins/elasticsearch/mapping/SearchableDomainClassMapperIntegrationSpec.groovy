package org.grails.plugins.elasticsearch.mapping

import grails.test.spock.IntegrationSpec
import org.grails.plugins.elasticsearch.ElasticSearchContextHolder
import test.Person

/**
 * Created by @marcos-carceles on 20/05/15.
 */
class SearchableDomainClassMapperIntegrationSpec extends IntegrationSpec {

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
        !incautiousMapping.propertiesMapping*.grailsProperty*.name.containsAll(["firstName","lastName","password"])

    }
}
