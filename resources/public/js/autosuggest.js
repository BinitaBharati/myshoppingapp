$(document).ready(function() {
 alert('in autosuggest11');
 $('#autosuggest').typeahead({
  hint: true,
  //Highlight the user given input chars in the search result menu.
  highlight: true,
  //Invoke ajax only after minimum of 3 chars has been typed by user.
  minLength: 3
},
{
  limit: 12,
  async: true,
  source: function (query, processSync, processAsync) {
    processSync([]);
    return $.ajax({
      url: "/autosuggest?keyword=" + $('#autosuggest').val(), 
      type: 'GET',
      data: {},
      dataType: 'json',
      success: function (json) {
        // in this example, json is simply an array of strings
		//alert('after invoking autosuggest ajax, received '+JSON.stringify(json));
        return processAsync(json.searchResult);
      }
    });
  }
});


});

