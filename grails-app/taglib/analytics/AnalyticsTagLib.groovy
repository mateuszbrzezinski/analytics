package analytics

class AnalyticsTagLib {
    static namespace = "analytics"

    def analyticsService


    def setTrackedPage = { attrs, body ->
        if (!attrs.page) {
            throwTagError("Tag [setTrackedPage] is missing required attribute [page]")
        }

        analyticsService.setTrackedPage(attrs.page)
    }

    def trackCode = { attrs, body ->
        log.debug("1")
        if (!analyticsService.isEnabled()){
            log.debug("2")
            return
        }
        
        def account = attrs.account ?: analyticsService.account

        def queue = analyticsService.fullQueue
        analyticsService.clear()
        def queueCode = ''

        if (queue && queue.size() > 0){
            queueCode = '_gaq.push(' + queue.collect{it.encodeAsJSON()}.join(',\n') + ');'
        }
        
        def code = """<script type="text/javascript">
  var _gaq = _gaq || [];
  _gaq.push(['_setAccount', '${account}']);
  ${queueCode}

  (function() {
    var ga = document.createElement('script'); ga.type = 'text/javascript'; ga.async = true;
    ga.src = ('https:' == document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';
    var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(ga, s);
  })();
</script>"""

        out << code
    }
}
