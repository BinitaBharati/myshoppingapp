import React, { Component } from "react";
import ReactDOM from "react-dom";
import Cookies from 'js-cookie';


/* 
Renders Bootstrap NavigationBar. A reference of this navigation bar is added to global
window variable, which is later accessed by LogInModalDialog component to set appropriate
states in the NavigationBar.
*/
class NavigationBar extends Component {
  constructor() {
	//Save logged in user details in state.
    super();
	var userEmail = "NA", userName = "NA", userType = "NA";
	var userCookie = Cookies.get('userCreds');
	alert('userCreds = '+userCookie);//email=anna@gmail.com;name=Anna+Williams;type=customer
	if (userCookie != null) {
		var emailIdx = userCookie.indexOf("email=");
		userEmail = userCookie.substring(emailIdx + "email=".length, userCookie.indexOf(";", emailIdx));
		var nameIdx = userCookie.indexOf("name=");
		userName = userCookie.substring(nameIdx + "name=".length, userCookie.indexOf(";", nameIdx));
		alert('userName = '+userName + ", userEmail = "+userEmail);
		var typeIdx = userCookie.indexOf("type=");
		userType = userCookie.substring(typeIdx + "type=".length, userCookie.indexOf(";", typeIdx));
	}
	
    this.state = {
      userEmail: userEmail,
	  userName: userName,
	  userType: userType
    };
  }
  
  
  render() {
	  alert('NavigationBar: render : state is '+JSON.stringify(this.state.userEmail));
	  if (this.state.userEmail !== 'NA') //not empty string means user is logged in.
	  {
		  return (
    <nav class="navbar navbar-expand-lg navbar-dark bg-dark fixed-top">
    <div class="container">
      <a class="navbar-brand" href="#">
		<h5 class="my-4">Binita's Shop</h5>	 
	  </a>	  
      <button class="navbar-toggler" type="button" data-toggle="collapse" data-target="#navbarResponsive" aria-controls="navbarResponsive" aria-expanded="false" aria-label="Toggle navigation">
        <span class="navbar-toggler-icon"></span>
      </button>
      <div class="collapse navbar-collapse" id="navbarResponsive">
		<form class="form-inline">
			<input class="form-control" type="text" placeholder="Search" aria-label="Search"/>
		</form>		
        <ul class="navbar-nav ml-auto">
          <li class="nav-item active" style={{ marginTop: "22px" }}>
            <a class="nav-link" href="#">Home
              <span class="sr-only">(current)</span>
            </a>
          </li>
		  <li class="nav-item dropdown">
			<a class="dropdown-toggle nav-link" data-toggle="dropdown" href="#">{this.state.userName}<br/>Account
				<span class="caret"></span>
			</a>
			<ul class="dropdown-menu">
				<li><a href="#">Page 1-1</a></li>
				<li><a href="#">Page 1-2</a></li>
				<li><a href="#">Page 1-3</a></li>
			</ul>
		  </li>
		  <li class="nav-item active" style={{ marginTop: "18px" }}>
            <a class="nav-link" href="#">
				<img class="img-fluid" src="assets/images/cart13.png" alt=""/>
            </a>
          </li>			
        </ul>
      </div>
    </div>
  </nav>
    );
	} else {
		return (
		<nav class="navbar navbar-expand-lg navbar-dark bg-dark fixed-top">
    <div class="container">
      <a class="navbar-brand" href="#">
		<h5 class="my-4">Binita's Shop</h5>	 
	  </a>	  
      <button class="navbar-toggler" type="button" data-toggle="collapse" data-target="#navbarResponsive" aria-controls="navbarResponsive" aria-expanded="false" aria-label="Toggle navigation">
        <span class="navbar-toggler-icon"></span>
      </button>
      <div class="collapse navbar-collapse" id="navbarResponsive">
		<form class="form-inline">
			<input class="form-control" type="text" placeholder="Search" aria-label="Search"/>
		</form>
        <ul class="navbar-nav ml-auto">
          <li class="nav-item active" style={{ marginTop: "22px" }}>
            <a class="nav-link" href="#">Home
              <span class="sr-only">(current)</span>
            </a>
          </li>      
		  <li class="nav-item" style={{ marginTop: "16px" }}>
            <a class="nav-link" href="#">
			     <button type="button" class="btn btn-primary btn-sm" data-toggle="modal" data-target="#loginPopUp">Log In</button>		                			 
			</a>
          </li>
		  <li class="nav-item active" style={{ marginTop: "18px" }}>
            <a class="nav-link" href="#">
				<img class="img-fluid" src="assets/images/cart13.png" alt=""/>
            </a>
          </li>			
        </ul>
      </div>
    </div>
  </nav>
  
		);
		
	}
    
  }
}

const wrapper = document.getElementById("navBarRoot");
//I need access to NavigationBar ref in LogInModalDialog. Ref : https://zhenyong.github.io/react/docs/more-about-refs.html
wrapper ? ReactDOM.render(<NavigationBar  ref={(compRef) => {window.navBarRef = compRef;alert('NavigationBar render : global ref = '+compRef)}}/>, wrapper) : false;
export default NavigationBar;


