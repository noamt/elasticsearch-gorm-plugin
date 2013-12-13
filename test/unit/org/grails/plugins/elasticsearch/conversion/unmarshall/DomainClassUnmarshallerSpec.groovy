package org.grails.plugins.elasticsearch.conversion.unmarshall

import org.codehaus.groovy.grails.commons.DefaultGrailsDomainClass
import org.codehaus.groovy.grails.commons.DefaultGrailsDomainClassProperty
import org.elasticsearch.common.bytes.BytesArray
import org.elasticsearch.common.text.StringAndBytesText
import org.elasticsearch.search.internal.InternalSearchHit
import org.elasticsearch.search.internal.InternalSearchHits
import org.grails.plugins.elasticsearch.ElasticSearchContextHolder
import org.grails.plugins.elasticsearch.mapping.SearchableClassMapping
import org.grails.plugins.elasticsearch.mapping.SearchableClassPropertyMapping
import spock.lang.Specification
import test.Building

import java.beans.PropertyDescriptor

class DomainClassUnmarshallerSpec extends Specification {
    void "An unmarshalled geo_point is marshalled into a GeoPoint domain object"() {
        def elasticSearchContextHolder = new ElasticSearchContextHolder()

        def buildingDomainClass = new DefaultGrailsDomainClass(Building)
        def locationMapping = new SearchableClassPropertyMapping(
            new DefaultGrailsDomainClassProperty(
                buildingDomainClass,
                new PropertyDescriptor('location', Building),
                [
                    mappingAtributes: null,
                    specialMappingAttributes: [geoPoint: true]
                ]
            )
        )
        def nameMapping = new SearchableClassPropertyMapping(new DefaultGrailsDomainClassProperty(buildingDomainClass, new PropertyDescriptor('name', Building)))

        def scm = new SearchableClassMapping(buildingDomainClass, [locationMapping, nameMapping])
        elasticSearchContextHolder.addMappingContext(scm)
        def unmarshaller = new DomainClassUnmarshaller(elasticSearchContextHolder: elasticSearchContextHolder)

        given: 'a search hit with a geo_point'

        InternalSearchHit[] hits = [new InternalSearchHit(0, '1', new StringAndBytesText('building'), new BytesArray('{"location":{"lat":53.0,"lon":10.0},"name":"WatchTower"}'), [:])]
        def searchHits = new InternalSearchHits(hits, 1, 0.1534264087677002f)

        when: 'an geo_point is unmarshalled'
        def results = unmarshaller.buildResults(searchHits)
        results.size() == 1
        then: 'this results in a GeoPoint domain object'
        results[0].name == "WatchTower"
        def location = results[0].location
        location.class == 'test.GeoPoint'
        location.lat == 53.0
        location.lon == 10.0
    }
}
