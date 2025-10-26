
// Function to add annotations from the form into the array and clear the inputs
function addAnnotation() {
    // Retrieve values from the form
    const deontic = document.getElementById('deontic').value.toUpperCase();
    const forEntity = document.getElementById('for').value.trim();
    const action = document.getElementById('to').value.trim();
    const conditionType1 = document.getElementById('condition1').value.toUpperCase();
    const conditionText1 = document.getElementById('conditionText1').value.trim();
	const conditionType2 = document.getElementById('condition2').value.toUpperCase();
    const conditionText2 = document.getElementById('conditionText2').value.trim();

    // Update the display in the annotations area
    document.getElementById('annotations-area').value += `------------------------\nIT IS ${deontic}\nFOR ${forEntity}\nTO ${action}\n`;
	
	if(conditionText1){document.getElementById('annotations-area').value += `${conditionType1} ${conditionText1}\n`;}
	if(conditionText2){document.getElementById('annotations-area').value += `${conditionType2} ${conditionText2}\n`;}

    // Clear form fields
    clearFormFields();
}

// Function to clear form fields after adding an annotation
function clearFormFields() {
    document.getElementById('deontic').selectedIndex = 0;
    document.getElementById('for').value = '';
    document.getElementById('to').value = '';
    document.getElementById('condition1').selectedIndex = 0;
    document.getElementById('conditionText1').value = '';
    document.getElementById('condition2').selectedIndex = 0;
    document.getElementById('conditionText2').value = '';
}

// Function to export annotations as a text file
function exportAnnotations() {
    
	let content = document.getElementById('annotations-area').value;

    // Create a Blob from the content
    const blob = new Blob([content], { type: 'text/plain' });
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = '2014_6_part_1.txt';
    document.body.appendChild(a);
    a.click();
    window.URL.revokeObjectURL(url);
    a.remove();
}
