

import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.*;
import javax.mail.internet.MimeMessage;

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


@WebServlet(
	    name = "CreatePollServlet",
	    urlPatterns = {"/createPollServlet"}
)
/**
 * 
 * @author GROUP 1
 * This is the create poll servlet. It accepts the poll interval(start and end), candidates for vote
 * and email of eligible voters.
 * All these entries will be stored in the Datastore.
 *
 */
public class CreatePollServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    public CreatePollServlet() {
        super();
    }
	
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	
	}
	
	/**
	 * Using post method, the details of poll are stored in database by calling respective methods.
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		PrintWriter pw = response.getWriter();

		int canRowCount = Integer.parseInt(request.getParameter("canRowCount"));
		int voteRowCount = Integer.parseInt(request.getParameter("voteRowCount"));
		
		String pollStartsDate = request.getParameter("pollStartsDate");
		String pollStartsTime = request.getParameter("pollStartsTime");
		String pollEndsDate = request.getParameter("pollEndsDate");
		String pollEndsTime = request.getParameter("pollEndsTime");

		for(int i=0; i<canRowCount; i++) {
			String firstName = request.getParameter("firstName"+i);
			String surname = request.getParameter("surname"+i);
			String faculty = request.getParameter("faculty"+i);
			addCandidate(firstName, surname, faculty);
		}
		
		for(int i=0; i<voteRowCount; i++) {
			String voterEmail = request.getParameter("emailid"+i);
			addVoterEmail(voterEmail);
		}
		
		Date pollStarts = null, pollEnds = null;
		try {
			pollStarts = new SimpleDateFormat("yyyy-MM-ddhh:mmZ").parse(pollStartsDate+pollStartsTime+"+0100");
			pollEnds = new SimpleDateFormat("yyyy-MM-ddhh:mmZ").parse(pollEndsDate+pollEndsTime+"+0100");
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		createPoll(pollStarts, pollEnds);
		
		pw.println("POLL CREATED");
		
		sendEmail();
		
		pw.println("Emails sent to eligible candidates with respective tokens");
	}
	
	/**
	 * This method accepts first name, surname and faculty of candidate and puts them in a 
	 * single Datastore Entity of Kind 'Candidate'.
	 * @param fn (firstname)
	 * @param sn (surname)
	 * @param fa (faculty)
	 */
	public void addCandidate(String fn, String sn, String fa) {
		
		Entity candidate = new Entity("Candidate");
		candidate.setProperty("FirstName", fn);
		candidate.setProperty("Surname", sn);
		candidate.setProperty("Faculty", fa);
		candidate.setProperty("Votes", 0);
		datastore.put(candidate);
		
	}
	
	/**
	 * This method accepts email address of eligible voter and puts them in a
	 * single Datastore Entity of Kind 'VoterEmail'.
	 * @param email
	 */
	public void addVoterEmail(String email) {
		
		Entity voterEmail = new Entity("VoterEmail");
		voterEmail.setProperty("Email", email);
		voterEmail.setProperty("Token", generateToken("ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890", 10));
		voterEmail.setProperty("HasVoted", false);
		datastore.put(voterEmail);
	}
	
	/**
	 * This method creates a poll by accepting start date and end date.
	 * It also creates a flag for triggering a reminder email through cron job so that the
	 * reminder email is triggered only if it has not been triggered yet.
	 * @param s (start date)
	 * @param e (end date)
	 */
	public void createPoll(Date s, Date e) {
		
		Entity pollInterval = new Entity("PollInterval");
		pollInterval.setProperty("PollStarts", s);
		pollInterval.setProperty("PollEnds", e);		
		datastore.put(pollInterval);
		
		Entity reminderFlag = new Entity("ReminderFlag");
		reminderFlag.setProperty("Flag", false);
		reminderFlag.setProperty("Reminder", false);
		datastore.put(reminderFlag);
	}
	
	/**
	 * This method send an invitation email to all eligible voters with a URL to cast vote.
	 * The URL also contains a valid token.
	 */
	public void sendEmail() {
		List<Entity> voterList = new ArrayList<Entity>();
		Query q = new Query("VoterEmail");
		voterList = datastore.prepare(q).asList(FetchOptions.Builder.withDefaults());
		String email;
		
		Properties props = new Properties();
	    Session mailSession = Session.getDefaultInstance(props, null); 
		
		for(Entity voter : voterList) {
	
		    email = (String)voter.getProperty("Email");
		    try {
		    	Message msg = new MimeMessage(mailSession);
		    	msg.setFrom(new InternetAddress("group1dsamws201819@gmail.com"));
		    	msg.addRecipient(Message.RecipientType.TO,
		                     new InternetAddress(email));
		    	msg.setSubject("Invitation to vote");
		    	msg.setText("Dear Candidate,\n"
		    			+ "You are cordially invited to cast your vote for our University Elections.  Please use the following link:\n"
		    			+ "group1assignment2ws1819.appspot.com/castVoteServlet?token="+(String)voter.getProperty("Token")
		    			+ "\nThank you for your participation.");
		    	Transport.send(msg);
		    } catch (AddressException ex) {} 
		    catch (MessagingException me) {}
		}
	}
	
	/**
	 * This method generates a random token to be given to each eligible voter.
	 * @param candidateChars (composition characters of token)
	 * @param length (length of the token)
	 * @return
	 */
	public String generateToken(String candidateChars, int length) {
	    StringBuilder sb = new StringBuilder();
	    Random random = new Random();
	    for (int i = 0; i < length; i++) {
	        sb.append(candidateChars.charAt(random.nextInt(candidateChars
	                .length())));
	    }
	    return sb.toString();
	}
}
