

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;

/**
 * This servlet displays the election summary as response.
 */
@WebServlet(
	    name = "ElectionSummaryServlet",
	    urlPatterns = {"/electionSummaryServlet"}
)
public class ElectionSummaryServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
    
    public ElectionSummaryServlet() {
        super();
    }

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    List<Entity> candidates = new ArrayList<Entity>();
    
    /**
     * This method displays the status of the poll which includes the summary if the poll has ended.
     */
    
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		PrintWriter pw = response.getWriter();
		
		Date currentDate = new Date();
		Entity pollInterval = datastore.prepare(new Query("PollInterval")).asList(FetchOptions.Builder.withDefaults()).get(0);
		Date pollStart = (Date)pollInterval.getProperty("PollStarts");
		Date pollEnd = (Date)pollInterval.getProperty("PollEnds");
		
		response.setContentType("text/html");
		
		Query q = new Query("Candidate").addSort("Votes", SortDirection.DESCENDING);
		candidates = datastore.prepare(q).asList(FetchOptions.Builder.withDefaults());
		
		pw.println("<html>"
				+ "<head>"
				+ "<title> Election Summary </title>"
				+ "</head>"
				+ "<body>"
				+ "<h1>ELECTION SUMMARY </h1>"
				+ "POLL STATUS: ");
		if(currentDate.before(pollStart)) {
			pw.println("The poll has not yet started.<br><h2>CURRENT STATUS</h2>");		
		}
		else if(currentDate.after(pollEnd)) {
			pw.println("The poll has ended.<br><h2>SUMMARY</h2>");
		}
		else {
			pw.println("The poll is on.<br><h2>CURRENT STATUS</h2>");
		}
		
		List<Entity> votersList = new ArrayList<Entity>();
		votersList = datastore.prepare(new Query("VoterEmail")).asList(FetchOptions.Builder.withDefaults());
		double voted = 0;
		double total = votersList.size();
		double hundred = 100;
		
		int casted = 0;
		for(Entity e : votersList) {
			if((boolean)e.getProperty("HasVoted")) {
				voted += 1;
				casted++;
			}
		}
		double percentageVoted = (voted/total)*hundred;
		
		pw.print("Total number of Voters: " + votersList.size() + "<br>");
		pw.print("Number of casted votes: " + casted + "<br>");
		pw.print("Percentage of voters who casted the vote: " + percentageVoted + "%" + "<br><br>"
				+ "<h4>CANDIDATES' DETAILS:</h4>");
		
		pw.print("<table>"
				+ "<tr>"
				+ "<td>"
				+ "FIRST NAME"
				+ "</td>"
				+ "<td>"
				+ "SURNAME"
				+ "</td>"
				+ "<td>"
				+ "FACULTY"
				+ "</td>"
				+ "<td>"
				+ "NUMBER OF VOTES"
				+ "</td>"
				+ "</tr>");
		for(int i=0; i<candidates.size(); i++) {
			Entity e = candidates.get(i);
			
			pw.print("<tr><td>" + (String)e.getProperty("FirstName") + "</td>" +  
					"<td>" + (String)e.getProperty("Surname") + "</td>" +
					"<td>" + (String)e.getProperty("Faculty") +"</td>" +
					"<td>" + (String)e.getProperty("Votes").toString() + "</td></tr>");
			pw.print("<br>");
		}
		
		pw.println("</table>");
		pw.println("</body>");
		pw.println("</html>");
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
