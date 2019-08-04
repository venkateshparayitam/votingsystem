<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="ISO-8859-1">
<title>Add Candidate</title>
</head>
<body>
<form action="/addCandidateServlet" method="post">
	<h1>ADD CANDIDATE</h1>
	<h4>First Name:</h4>
	<input type="text" name="firstName"><br>
	<h4>Surname:</h4>
	<input type="text" name="surname"><br>
	<h4>Faculty:</h4>
	<input type="text" name="faculty"><br>
	<input type="submit">
	
</form>
</body>
</html>