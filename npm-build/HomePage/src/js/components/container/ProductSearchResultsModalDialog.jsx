import React, { Component, Fragment } from "react";
import ReactDOM from "react-dom";
import ModalDialog2 from "../presentational/ModalDialog2.jsx";
import Input from "../presentational/Input.jsx";
import Button from "../presentational/Button.jsx";
import axios from 'axios';
//import ReactHtmlParser, { processNodes, convertNodeToElement, htmlparser2 } from 'react-html-parser';
 

/* 
	Renders ProductSearchResults Modal Dialog. This modal does not get self-render like LoginModalDialog.	
	The invocation to ReactDOM.render is not present in this file unlike LoginModalDialog.jsx 
	(LogInModalDialog gets rendered by deafult, and gets visisble/hidden as and when required). This particular 
	component gets created explicitly through NavigationBar.jsx. So, while running 'npm run build', we need to
	only build NavigationBar.jsx. As part of NavigationBar.jsx, this component will get automatically built. Also,
	as this file does not render a component by itself, this need not be built seperately and included in the home page.
	Note LoginModalDialog corresponding js does get included in the home explicitly.This should not be, only navbar.js
	whch actually renders this component should be included.
*/
class ProductSearchResultsModalDialog extends Component {
  constructor(props) {
	  super(props);
	//Save logged in user details in state.
	alert('ProductSearchResultsModalDialog: constructor entered with props = '+this.props);
    
	
	 //this.handleInputTextChange = this.handleInputTextChange.bind(this);
    this.generateProductSearchResultsModalBody = this.generateProductSearchResultsModalBody.bind(this);
	//this.generateModalErrorMsgDiv = this.generateModalErrorMsgDiv.bind(this);
	this.modalCloseBtnOnClickHandler = this.modalCloseBtnOnClickHandler.bind(this);
	//this.handleErrMsgCloseAction = this.handleErrMsgCloseAction.bind(this);
	
	this.state = {
      searchResults: props.searchResultJson,
	  rankMap : {"5" : <Fragment>&#9733; &#9733; &#9733; &#9733; &#9733;</Fragment> , "4" : <Fragment>&#9733; &#9733; &#9733; &#9733; &#9734;</Fragment>, 
	  "3" : <Fragment>&#9733; &#9733; &#9733; &#9734; &#9734;</Fragment> , "2" : <Fragment>&#9733; &#9733; &#9734; &#9734; &#9734;</Fragment>, 
	  "1" : <Fragment>&#9733; &#9734; &#9734; &#9734; &#9734;</Fragment>}
    };
   
  }
  
  componentDidMount(){
	//Hack to open the bootstrap4 modal without clicking a button.
	document.body.className  = 'modal-open';
  }

 componentWillUnmount(){
	//Hack to close the bootstrap4 modal.
	document.body.className = document.body.className.replace("modal-open","");
 } 
  
  
  setSearchResultAjaxResponse(searchResults) {
	  this.setState.searchResults = searchResults;
	  makeModalVisible();
  }
  
  handleInputTextChange(event) {
    this.setState({ [event.target.id]: event.target.value });
  }
  
  constructSearchResultHtml() {
	  
  }
  
  generateProductSearchResultsModalBody() {
    //alert("generateProductSearchResultsModalBody444: entered with "+JSON.stringify(this.state.searchResults));	
	
    return (      	
           <div class="row my-4">
		   {
			   this.state.searchResults.data.searchResult.map(function (eachProductInfo){
			   //alert('searchResultAjaxResponseJson: entered with eachProductInfo = '+JSON.stringify(eachProductInfo));
			   //alert('hurr: '+this.state.rankMap[eachProductInfo.rank]);
			   return (<div class="col-lg-6 col-md-8 mb-6">
                           <div class="card h-100">
			                     <div class="card-body">
                                      <a href="#">
											<img class="card-img-top" src={"assets/images/" + eachProductInfo.pictures} alt=""/>
                                      </a>			  
			                          <h4 class="card-title">
											<a href="#">{eachProductInfo.name}</a>
                                      </h4>		
                                      <h5>{eachProductInfo.price}</h5>
                                      <p class="card-text">{eachProductInfo.name}</p>								  
								</div>
								<div class="card-footer">
									<small class="text-muted">{ this.state.rankMap[eachProductInfo.rank] }</small>
								</div>
                          </div>
                        </div>);
		   }, this)}
		   </div>
		
		   
		   
    );	
   
  }
  
  
  modalCloseBtnOnClickHandler(event) {
		event.preventDefault();
        //alert('modalCloseBtnOnClickHandler: entered');	
        ReactDOM.unmountComponentAtNode(document.getElementById("productSearchResultsDisplayModalRoot"));		
			
  }
  
 
  render() {
	document.body.style.class="modal-open";  
	return (<ModalDialog2 klass="modal fade show" id="productSearchResultsPopUp" ariaLabel="productSearchResultsPopUpCenterTitle" modalBody={this.generateProductSearchResultsModalBody()} modalCloseBtnOnClickHandler={this.modalCloseBtnOnClickHandler} cssStyle={{display : 'block'}}/>);
	
	
    
  }
  
  
}

export default ProductSearchResultsModalDialog;
