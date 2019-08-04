

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
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;


@WebServlet(
	    name = "CastVoteServlet",
	    urlPatterns = {"/castVoteServlet"}
)
/**
 * This servlet enables all eligible voters to cast their vote.
 * @author GROUP 1
 *
 */
public class CastVoteServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private String token = null;
	boolean tokenValidity = false;
	boolean tokenMatches = false;
	
	List<Entity> voterList = new ArrayList<Entity>();
	
	Entity currentVoter = new Entity("VoterEmail");
	  
    public CastVoteServlet() {
        super();
    }

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    List<Entity> candidates = new ArrayList<Entity>();
	
    /**
     * This method is called when the voter clicks on the link provided in the email.
     * This method then processes the token, checks whether the poll is on and sends a response.
     * If the poll is on the voter gets voting page.
     */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		PrintWriter pw = response.getWriter();
		
		Date currentDate = new Date();
		Entity pollInterval = datastore.prepare(new Query("PollInterval")).asList(FetchOptions.Builder.withDefaults()).get(0);
		Date pollStart = (Date)pollInterval.getProperty("PollStarts");
		Date pollEnd = (Date)pollInterval.getProperty("PollEnds");
		
		if(currentDate.before(pollStart)) {
			pw.println("The poll has not yet started.");		
		}
		else if(currentDate.after(pollEnd)) {
			pw.println("The poll has ended.");
		}
		else {
			token = request.getParameter("token");
			
			
			Query q = new Query("VoterEmail");
			voterList = datastore.prepare(q).asList(FetchOptions.Builder.withDefaults());
			
			for(Entity e : voterList) {
				if((token.equals((String)e.getProperty("Token")))) {
					tokenMatches = true;	
					if(!((boolean)e.getProperty("HasVoted"))) {
							tokenValidity = true;
							currentVoter = e;
							break;
					}
				}	
			}
			
			response.setContentType("text/html");
			
			if(!tokenMatches) {
				pw.println("Invalid Token");
			}
			else if(!tokenValidity) {
				pw.println("You have already voted.");
			}
			else {
				
				q = new Query("Candidate");
				candidates = datastore.prepare(q).asList(FetchOptions.Builder.withDefaults());
				
				pw.println("<html>"
						+ "<head>"
						+ "<title> Welcome to the polling booth </title>"
						+ "</head>"
						+ "<body>"
						+ "<h1>WELCOME TO THE POLLING BOOTH OF THIS UNIVERSITY </h1>"
						+ "<form action=\"/castVoteServlet\" method=\"post\">"
						+ "Please select your favorite candidate: <br>");
				for(int i=0; i<candidates.size(); i++) {
					Entity e = candidates.get(i);
					pw.println("<input type=\"radio\" name=\"choice\" value=\""+e.getKey().getId()+"\">");
					
					pw.println((String)e.getProperty("FirstName") + " " +
							(String)e.getProperty("Surname") + " " +
							(String)e.getProperty("Faculty"));
					pw.println("<br>");
				}
				
				pw.println("<input type=\"submit\" value=\"CAST MY VOTE\">");
				pw.println("</form>");
				pw.println("</body>");
				pw.println("</html>");
				
			}
		}
		
		
	}

	/**
	 * This method takes the input of the selected candidate, updates the vote count of the candidate
	 * and displays a message response that the vote has been recorded only if the token is valid and
	 * the poll is on.
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		PrintWriter pw = response.getWriter();
		try {
			Long selectedCandidate = Long.parseLong(request.getParameter("choice"));
			Key k = KeyFactory.createKey("Candidate", selectedCandidate);
			Entity e = new Entity("Candidate");
			e = datastore.get(k);
			if(tokenMatches && tokenValidity) {
				long votes = (long)e.getProperty("Votes");
				votes += 1;
				e.setProperty("Votes", votes);
				datastore.put(e);
				
				currentVoter.setProperty("HasVoted", true);
				datastore.put(currentVoter);
				pw.println("You have voted for " + (String)e.getProperty("FirstName") + " " + (String)e.getProperty("Surname") + " from Faculty " + (String)e.getProperty("Faculty") + ". ");
				pw.println("Your vote has been recorded.  Thank you for your vote.");
			}
			
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
		
	}	

}
