package org.grails.plugins.elasticsearch.mapping

import org.codehaus.groovy.grails.commons.DefaultGrailsDomainClass
import org.codehaus.groovy.grails.commons.GrailsDomainClass
import spock.lang.Specification
import test.transients.Color
import test.transients.Palette

/**
 * Created by @marcos-carceles on 27/07/15.
 */
class ElasticSearchMappingFactorySpec extends Specification {

    void "identifies correct component classes"() {
        given:
        GrailsDomainClass domainClass = new DefaultGrailsDomainClass(clazz)
        expect:
        ElasticSearchMappingFactory.getReferencedType(domainClass.getPropertyByName(property)) == referencedClass

        where:
        clazz   | property              || referencedClass
        Palette | 'author'              || String
        Palette | 'colors'              || Color
        Palette | 'tags'                || String
        Palette | 'complementaryColors' || Color

    }
}
