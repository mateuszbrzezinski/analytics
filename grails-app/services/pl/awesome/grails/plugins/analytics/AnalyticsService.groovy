package pl.awesome.grails.plugins.analytics

import org.codehaus.groovy.grails.web.servlet.mvc.GrailsWebRequest
import org.springframework.web.context.request.RequestContextHolder
import org.codehaus.groovy.grails.web.servlet.FlashScope
import org.codehaus.groovy.grails.commons.ConfigurationHolder

class AnalyticsService {
    static transactional = false

    String trackedPage

    def setTrackedPage(String page) {
        getFlashScope().trackedPage = page
    }
    
    String getTrackedPage(){
        return getFlashScope().trackedPage
    }

    def getFullQueue(){
        def queue = []
        queue.addAll(gaq)
        queue << createTrackPageViewElement()

        return queue
    }

    def trackPageView(String page = null){
        def gaqTmp = gaq

        if(gaqTmp.find { it[0] == '_trackPageview'}){
            log.warn("TrackPageView already queued")
        }
        
        def pageView = createTrackPageViewElement(page)

        gaqTmp << pageView
    }

    private ArrayList<String> createTrackPageViewElement(String page = null) {
        def pageView = ['_trackPageview']

        if (page != null) {
            pageView << page
        } else if (getTrackedPage() != null) {
            pageView << getTrackedPage()
        }
        return pageView
    }

    def setCustomVar(int index, String name, String value, Integer scope = null){
        def gaqTmp = gaq

        if(gaqTmp.find { it[0] == '_setCustomVar' && it[1] == index}){
            log.warn("Overriding customVar idx: $index")
        }

        def var = ['_setCustomVar', index, name, value]
        
        if(scope != null){
            var << scope
        }

        gaqTmp << var
    }

    def trackEvent(String category, String action, String label = null, Integer value = null, Boolean nonInteraction = null){
        def event = ['_trackEvent', category, action]
        
        if(label != null){
            event << label
        }
        
        if(value != null){
            event << value
        }

        if(nonInteraction != null){
            event << nonInteraction
        }

        gaq << event
    }

    def getAccount(){
        ConfigurationHolder.config.analytics.account
    }

    def clear(){
        getFlashScope().gaq = null
        trackedPage = null
    }

    def isEnabled(){
        if(!getAccount()){
            return false
        }
        
        if(ConfigurationHolder.config.analytics.enabled != false){
            return true
        }

        return false
    }

    protected def getGaq(){
        def flash = getFlashScope()
        if(flash.gaq == null){
            flash.gaq = new LinkedHashSet()
        }

        return flash.gaq
    }

    protected FlashScope getFlashScope(){
        GrailsWebRequest webRequest = (GrailsWebRequest) RequestContextHolder.currentRequestAttributes();
        def flash = webRequest.flashScope

        return flash
    }
}
