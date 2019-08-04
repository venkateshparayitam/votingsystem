

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
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

/**
 * This servlet is activated only using a cron job.
 */
@WebServlet(
	    name = "ReminderEmailServlet",
	    urlPatterns = {"/reminderEmailServlet"}
)
public class ReminderEmailServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ReminderEmailServlet() {
        super();
        // TODO Auto-generated constructor stub
    }
    
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    

	/**
	 * This method checks if the reminder email has not yet been triggered and calls method sendReminderEmail().
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		
		Entity reminderFlag = datastore.prepare(new Query("ReminderFlag")).asList(FetchOptions.Builder.withDefaults()).get(0);
		boolean flag = (boolean)reminderFlag.getProperty("Flag");
		
		if(!flag) {
			Date currentDate = new Date();
			List<Entity> pollIntervals = new ArrayList<Entity>();
			Query query = new Query("PollInterval");
			pollIntervals = datastore.prepare(query).asList(FetchOptions.Builder.withDefaults());
			Entity pollInterval = pollIntervals.get(0);
			Date pollStart = (Date)pollInterval.getProperty("PollStarts");
			
			LocalDate current = currentDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
			LocalDate pollStarts = pollStart.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
			
			int year = (pollStarts.getYear()) - (current.getYear());
			int month = (pollStarts.getMonthValue()) - (current.getMonthValue());
			int day = (pollStarts.getDayOfMonth()) - (current.getDayOfMonth());		
			
			if(year==0 && month==0 && day==1) {
				sendReminderEmail();
				reminderFlag.setProperty("Flag", true);
				datastore.put(reminderFlag);
			}
		}
		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}
	
	/**
	 * This method sends the reminder email to eligible voters with the link to cast vote including token.
	 */
	public void sendReminderEmail() {
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
		    	msg.setSubject("REMINDER TO VOTE");
		    	msg.setText("Dear Voter,\n"
		    			+ "This is a gentle reminder to cast your vote(you can use the link down below). Please ignore if you have already voted. Thanks!\n"
		    			+ "group1assignment2ws1819.appspot.com/castVoteServlet?token="+(String)voter.getProperty("Token"));
		    	Transport.send(msg);
		    } catch (AddressException ex) {  	} 
		    catch (MessagingException me) {   	}
		}
	}
}
