package servlets

import models.Song
import repository.SongsRepository
import repository.SongsRepositoryImpl
import java.text.SimpleDateFormat
import java.util.*
import javax.servlet.annotation.WebServlet
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * This servlet shows list of songs (can accept parameters to add new songs, find songs of defined artist)
 * Also you can play tracks if server has audio files for it
 * @property repository songs repository
 */
@WebServlet(name = "SongsList", value = ["/songs"])
class SongsListServlet : HttpServlet() {


    private val repository: SongsRepository = SongsRepositoryImpl()

    /**
     * GET request handler
     * @param req http request
     * @param res http response
     * @return Unit
     */
    override fun doGet(req: HttpServletRequest, res: HttpServletResponse) {
        //Uncomment what you want to use
        //jspFlow(req, res)
        defaultFlow(req, res)
    }

    /**
     * Use Java Server pages for web application
     * @param req http request
     * @param res http response
     * @return Unit
     */
    private fun jspFlow(req: HttpServletRequest, res: HttpServletResponse) {
        res.sendRedirect(JSP_FILE_PATH)
    }

    /**
     * Use default flow for web application (using writer)
     * @param req http request
     * @param res http response
     * @return Unit
     */
    private fun defaultFlow(req: HttpServletRequest, res: HttpServletResponse) {
        req.characterEncoding = "utf-8"
        res.contentType = "text/html;charset=UTF-8"
        //Getting lang (saved cookie)
        val lang = req.getParameter("lang") ?: getLangFromCookiesOrNull(req) ?: "en"
        res.addCookie(Cookie("lang", lang))

        val artist = req.getParameter("artist")
        val visitCount = req.session.getAttribute("count") as? Int ?: 0
        val visitDate = req.session.getAttribute("date") as? String ?: getCurrentDate()

        val content = buildString {
            val resources = ResourceBundle.getBundle("Song", Locale(lang))
            append("<html>")
            append("<head>")
            append("<link rel=\"stylesheet\" type=\"text/css\" href=\"styles/style.css\">")
            append("<link href=\"https://fonts.googleapis.com/css?family=Roboto&display=swap\" rel=\"stylesheet\">")
            append("<link rel=\"stylesheet\" href=\"https://unpkg.com/purecss@1.0.1/build/pure-min.css\" integrity=\"sha384-oAOxQR6DkCoMliIh8yFnu25d7Eq/PHS21PClpwjOTeU2jRSq11vu66rf90/cZr47\" crossorigin=\"anonymous\">")
            append("<title>${resources.getString("header")}</title>")
            append("</head>")
            append("<body>")
            append("<h1>${resources.getString("list")} ${artist ?: ""}</h1>")
            append("<table class=\"pure-table\">")
            append("<tr>")
            append("<th><b>${resources.getString("title")}</b></th>")
            append("<th><b>${resources.getString("album")}</b></th>")
            append("<th><b>${resources.getString("year")}</b></th>")
            append("<th><b>${resources.getString("artist")}</b></th>")
            append("<th><b>${resources.getString("genre")}</b></th>")
            append("<th><b>${resources.getString("play")}</b></th>")
            append("</tr>")
            repository.getAllSongs()
                .filter { artist.isNullOrBlank() || artist == it.artist }
                .forEach { append(it.toHtml()) }
            append("</table>")
            append("<h3>${String.format(resources.getString("visit_date"), visitDate)}</h3>")
            append("<h3>${String.format(resources.getString("visit_count"), visitCount)}")
            append("</body>")
            append("</html>")
        }
        res.writer.write(content)
        //save visit count and date
        req.session.setAttribute("count", visitCount + 1)
        req.session.setAttribute("date", getCurrentDate())
    }

    private fun getCurrentDate() = SimpleDateFormat(DATE_PATTERN).format(Date())

    private fun getLangFromCookiesOrNull(req: HttpServletRequest) =
        req.cookies?.firstOrNull { it.name == "lang" }?.value

    /**
     * POST request handler
     * @param req http request
     * @param resp http response
     * @return Unit
     */
    override fun doPost(req: HttpServletRequest, resp: HttpServletResponse?) {
        req.characterEncoding = "utf-8"
        val title = req.getParameter("title")
        val album = req.getParameter("album")
        val year = req.getParameter("year").toInt()
        val artist = req.getParameter("artist")
        val genre = req.getParameter("genre")
        repository.addSong(Song(title, album, year, artist, genre))
    }

    companion object {
        private const val JSP_FILE_PATH = "SongList.jsp"
        const val DATE_PATTERN = "dd/M/yyyy hh:mm:ss"
    }
}