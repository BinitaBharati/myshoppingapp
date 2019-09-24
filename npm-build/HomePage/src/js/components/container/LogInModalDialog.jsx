import React, { Component } from "react";
import ReactDOM from "react-dom";
import ModalDialog from "../presentational/ModalDialog.jsx";
import Input from "../presentational/Input.jsx";
import Button from "../presentational/Button.jsx";
import axios from 'axios';

/* 
	Renders LogIn Modal Dialog.
*/
class LogInModalDialog extends Component {
  constructor() {
	//Save logged in user details in state.
    super();
	
	this.state = {
      inputEmail: "",
	  inputPassword: "",
	  showErrorMsg: false,
	  errorMsg: "",
	  showModal : false
    };
    this.handleInputTextChange = this.handleInputTextChange.bind(this);
    this.generateLogInModalBody = this.generateLogInModalBody.bind(this);
	this.generateModalErrorMsgDiv = this.generateModalErrorMsgDiv.bind(this);
	this.handleButtonAction = this.handleButtonAction.bind(this);
	this.handleErrMsgCloseAction = this.handleErrMsgCloseAction.bind(this);
  }
  
  handleInputTextChange(event) {
    this.setState({ [event.target.id]: event.target.value });
  }
  
  generateLogInModalBody() {	  
    return (
      <form class="form-signin" id="login-form">
        <Input
          text="Email"
          label="inputEmail"
          type="email"
          id="inputEmail"
          value={this.state.inputEmail}
          handleChange={this.handleInputTextChange}
		  name="inputEmail"
		  placeHolder="Email address"		  
        />
		<Input
          text="Password"
          label="inputPassword"
          type="password"
          id="inputPassword"
          value={this.state.inputPassword}
          handleChange={this.handleInputTextChange}
		  name="inputPassword"
		  placeHolder="Password"		  
        />		
      </form>
    );
  }
  
  generateModalErrorMsgDiv() {	  
    return (
     <div id="errMsgDivContent" style={{display: this.state.showErrorMsg ? 'block' : 'none' }} class="alert alert-info" role="alert" >
		<strong>{this.state.errorMsg}</strong>
		<button type="button" class="close" id="closeErrMsgBtn" onClick={this.handleErrMsgCloseAction}>
			<span aria-hidden="true">&times;</span>
		</button>
	</div>
    );
  }
  
  handleButtonAction(event) {
	      event.preventDefault();
		  alert('gggggggghandleButtonAction: entered inputEmail = '+this.state.inputEmail + ' showErrMsg = '+this.state.showErrorMsg);
		  /* By default axios serializes JavaScript objects to JSON (ie content-type: application/json)
		   */
          axios({
				method: 'post',
				headers: {
					'Content-Type': 'application/json'
				},
				url: '/loginsubmit3',
				data: {'inputEmail' : this.state.inputEmail, 'inputPassword' : this.state.inputPassword}
			})
			.then(response => {
				alert("response is  = "+JSON.stringify(response));
				if (response.data.status == 'ERROR') {
					this.setState({'showErrorMsg' : true, 'errorMsg' : response.data.msg});
				} else {
					alert('handleButtonAction: Getting navBarRef = '+window.navBarRef);
					window.navBarRef.setState( {userEmail: this.state.inputEmail,
												userName: response.data.userName,
												userType: response.data.userType});
					this.setState({showModal : true});//Its already set to false, so UI need to set it to true and then back to false for the modal to dissapear.
					this.setState({showModal : false});
				}
				
			})
			.catch(function (error) {
				alert(error.message);
		    });
			
			
  }
  
  handleErrMsgCloseAction(event) {
	      event.preventDefault();
		  alert('handleErrMsgCloseAction: entered errorMsg = '+this.state.errorMsg + " showErrMsg = "+this.state.showErrorMsg);
		  this.setState({'showErrorMsg' : false, 'errorMsg' : ''});	
  }
  
  render() {
	  
    return (<ModalDialog klass={this.state.showModal ? 'modal fade show' : 'modal fade'} id="loginPopUp" ariaLabel="loginPopUpCenterTitle" modalBody={this.generateLogInModalBody()}
	errorMsgJsx={this.generateModalErrorMsgDiv()} actionButtonLabel="Sign In" actionBtnOnClickHandler={this.handleButtonAction}/>	);
	
    
  }
}

const wrapper = document.getElementById("loginModalRoot");
wrapper ? ReactDOM.render(<LogInModalDialog />, wrapper) : false;
export default LogInModalDialog;
