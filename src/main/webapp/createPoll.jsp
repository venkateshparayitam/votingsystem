<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>


<!DOCTYPE html>
<html>
<head>
<meta charset="ISO-8859-1">
<title>Create Poll</title>
</head>
<style>
body {font-family: Arial;}

/* Style the tab */
.tab {
  overflow: hidden;
  border: 1px solid #ccc;
  background-color: #f1f1f1;
}

/* Style the buttons inside the tab */
.tab button {
  background-color: inherit;
  float: left;
  border: none;
  outline: none;
  cursor: pointer;
  padding: 14px 16px;
  transition: 0.3s;
  font-size: 17px;
}

/* Change background color of buttons on hover */
.tab button:hover {
  background-color: #ddd;
}

/* Create an active/current tablink class */
.tab button.active {
  background-color: #ccc;
}

/* Style the tab content */
.tabcontent {
  display: none;
  padding: 6px 12px;
  border: 1px solid #ccc;
  border-top: none;
}

table {
  font-family: arial, sans-serif;
  border-collapse: collapse;
  width: 100%;
}

td, th {
  border: 1px solid #dddddd;
  text-align: left;
  padding: 8px;
}

tr:nth-child(even) {
  background-color: #dddddd;
}

</style>

<body>
<form action="/createPollServlet" method="post">
	<h2>CREATE A POLL</h2>
	<div class="tab">
  		<button type="button" class="tablinks" onclick="openTab(event, 'PollingTimeInterval')" id="defaultOpen">Polling Time Interval</button>
  		<button type="button" class="tablinks" onclick="openTab(event, 'AddCandidates')">Add Candidates</button>
  		<button type="button" class="tablinks" onclick="openTab(event, 'AddVotersEmails')">Add Voters' Emails</button>
	</div>

	<div id="PollingTimeInterval" class="tabcontent">
  		<h3>POLLING TIME INTERVAL</h3>
		Poll Starts:<br>
		<input type="date" name="pollStartsDate"><input type="time" name="pollStartsTime" ><br><br>
		Poll Ends:<br>
		<input type="date" name="pollEndsDate"><input type="time" name="pollEndsTime" ><br><br>
	</div>

	<div id="AddCandidates" class="tabcontent">
  		<h3>ADD CANDIDATE</h3>
		<table id="candidateTable">
			<tr>
		    <th>First Name</th>
		    <th>Last Name</th>
		    <th>Faculty</th>
			</tr>
			<tr>
				<td>
				<input type="text" name="firstName0">
				</td>
				<td>
				<input type="text" name="surname0">
				</td>
				<td>
				<input type="text" name="faculty0">
				</td>
				
			</tr>
		</table>
		<table>
		<tr>
			<td><input type="button" onclick="insertCandidateRow();" value="Insert Row"></td>
		</tr>
	
		</table>
		
		<p></p>
		<h3>LIST OF CANDIDATES</h3>
		<table>
		<tr>
		    <th>First Name</th>
		    <th>Last Name</th>
		    <th>Faculty</th>
		</tr>
		</table>
	</div>

	<div id="AddVotersEmails" class="tabcontent">
  		<h3>ADD VOTER'S EMAILS</h3>
		<table id="votersTable">
			<tr>
		    <th>Voter Email</th>
			</tr>
			<tr>
				<td><input type="text" name="emailid0"></td>
			</tr>
		</table>
		<table>
	 	<tr>
			<td><input type="button" onclick="insertVoterRow();" value="Insert Row"></td>
		</tr>
	
		</table>
	</div>
	<br>
	<input type="hidden" name="canRowCount" id="canRowCount" value="0"/>
	<input type="hidden" name="voteRowCount" id="voteRowCount" value="0"/>
	<input type="submit" onclick="checkCountRow();" >
</form>

<script>

function openTab(evt, cityName) {
  // Declare all variables
  var i, tabcontent, tablinks;

  // Get all elements with class="tabcontent" and hide them
  tabcontent = document.getElementsByClassName("tabcontent");
  for (i = 0; i < tabcontent.length; i++) {
    tabcontent[i].style.display = "none";
  }

  // Get all elements with class="tablinks" and remove the class "active"
  tablinks = document.getElementsByClassName("tablinks");
  for (i = 0; i < tablinks.length; i++) {
    tablinks[i].className = tablinks[i].className.replace(" active", "");
  }

  // Show the current tab, and add an "active" class to the button that opened the tab
  document.getElementById(cityName).style.display = "block";
  evt.currentTarget.className += " active";
} 

document.getElementById("defaultOpen").click();

// add row to the candidate table dynamically
var index_candidate = 1;
function insertCandidateRow() {
	var can_table = document.getElementById("candidateTable");
    var can_row = can_table.insertRow(can_table.rows.length);
    var can_cell1 = can_row.insertCell(0);
    var can_t1 = document.createElement("input");
    	can_t1.name = "firstName"+index_candidate;
    	can_cell1.appendChild(can_t1);
    var can_cell2 = can_row.insertCell(1);
    var can_t2 = document.createElement("input");
    	can_t2.name = "surname"+index_candidate;
    	can_cell2.appendChild(can_t2);
    var can_cell3 = can_row.insertCell(2);
    var can_t3 = document.createElement("input");
    	can_t3.name = "faculty"+index_candidate;
    	can_cell3.appendChild(can_t3);
    	
    	
    	index_candidate++;
    	
}



//add row to the voters table dynamically
var index_voters = 1;
function insertVoterRow() {
	var vote_table = document.getElementById("votersTable");
    var vote_row = vote_table.insertRow(vote_table.rows.length);
    var vote_cell1 = vote_row.insertCell(0);
    var vote_t1 = document.createElement("input");
    	vote_t1.name = "emailid"+index_voters;
    	vote_cell1.appendChild(vote_t1);
  
    	index_voters++;
}


function checkCountRow() {
   
    
	document.getElementById('canRowCount').value = index_candidate;
	document.getElementById('voteRowCount').value = index_voters;
	
}

</script>
</body>
</html>