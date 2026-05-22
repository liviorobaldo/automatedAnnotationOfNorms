
function extractSectionFromIframe() {
    const iframe = document.getElementById('myIframe');
    
    // Check if iframe exists
    if (!iframe) {
        console.error("Iframe not found!");
        return 'unknown';
    }

    // Wait for iframe to load
    iframe.addEventListener('load', () => {
        try {
            // Access iframe content
            const iframeDocument = iframe.contentDocument || iframe.contentWindow.document;

            // Find the section element
            const sectionElement = iframeDocument.querySelector('.LegAnchorID, .LegDS, span[id^="section-"]');
            const sectionId = sectionElement ? sectionElement.id : 'unknown';
            console.log('Current section ID:', sectionId);
            return sectionId;
        } catch (error) {
            console.error("Unable to access iframe content. Ensure it is from the same origin.");
            return 'unknown';
        }
    });
}
// This function will be triggered every time the iframe is scrolled
function trackVisibleSection() {
    const iframe = document.getElementById('myIframe');
    
    if (!iframe) {
        console.error("Iframe not found!");
        return;
    }

    iframe.addEventListener('load', () => {
        try {
            const iframeDocument = iframe.contentDocument || iframe.contentWindow.document;
            const sections = iframeDocument.querySelectorAll('.LegAnchorID, span[id^="section-"], div[id^="section-"]');

            if (sections.length === 0) {
                console.error("No sections found in the iframe content.");
                return;
            }

            const iframeRect = iframe.getBoundingClientRect();
            const iframeScrollTop = iframe.contentWindow.scrollY || iframe.contentDocument.documentElement.scrollTop;

            let visibleSection = null;
            sections.forEach((section) => {
                const sectionRect = section.getBoundingClientRect();
                if (sectionRect.top >= iframeRect.top && sectionRect.bottom <= iframeRect.bottom) {
                    visibleSection = section.id;
                    console.log('Currently visible section:', visibleSection);
                }
            });

            if (visibleSection === null) {
                console.log("No section is currently in view.");
            }
        } catch (error) {
            console.error("Unable to access iframe content. Ensure it is from the same origin.");
        }
    });
}

// This function will listen for the 'scroll' event and trigger the tracking function
function setupScrollTracking() {
    const iframe = document.getElementById('myIframe');
    
    if (!iframe) {
        console.error("Iframe not found!");
        return;
    }

    // Listen to the scroll event on the iframe's content
    iframe.addEventListener('load', function() {
        console.log("Iframe content loaded successfully!");
        
        // Now set up scroll tracking for the iframe content
        iframe.contentWindow.addEventListener('scroll', trackVisibleSection);
    });
}

// Global variable to store the last selected text
let lastSelectedText = '';
let selectionIndicator = null;

function showSelectionIndicator(iframe) {
    // Remove existing indicator if any
    if (selectionIndicator) {
        selectionIndicator.remove();
    }

    // Create new indicator
    selectionIndicator = document.createElement('div');
    selectionIndicator.style.position = 'fixed';
    selectionIndicator.style.right = '10px';
    selectionIndicator.style.top = '10px';
    selectionIndicator.style.padding = '10px';
    selectionIndicator.style.background = '#4CAF50';
    selectionIndicator.style.color = 'white';
    selectionIndicator.style.borderRadius = '5px';
    selectionIndicator.style.zIndex = '1000';
    selectionIndicator.style.cursor = 'pointer';
    selectionIndicator.style.boxShadow = '0 2px 5px rgba(0,0,0,0.2)';
    selectionIndicator.textContent = 'Text selected! Click here to use';
    
    selectionIndicator.onclick = function() {
        try {
            lastSelectedText = iframe.contentWindow.getSelection().toString();
            console.log('Selected text captured:', lastSelectedText);
            selectionIndicator.remove();
        } catch (e) {
            console.error('Error capturing selection:', e);
        }
    };
    
    document.body.appendChild(selectionIndicator);
}

document.addEventListener("DOMContentLoaded", function() {
    setupScrollTracking();
    
    const iframe = document.getElementById('myIframe');
    iframe.addEventListener('load', function() {
        try {
            // Set up a mutation observer to watch for selection changes
            const iframeDoc = iframe.contentDocument || iframe.contentWindow.document;
            iframeDoc.addEventListener('selectionchange', function() {
                const selection = iframe.contentWindow.getSelection();
                if (selection && selection.toString().trim()) {
                    showSelectionIndicator(iframe);
                } else if (selectionIndicator) {
                    selectionIndicator.remove();
                }
            });
        } catch (e) {
            console.error('Error setting up selection observer:', e);
        }
    });
});

function addAnnotation() {
    console.log('addAnnotation function called');
    // Retrieve values from the form
    const deontic = document.getElementById('deontic').value.toUpperCase();
    const forEntity = document.getElementById('for').value.trim();
    const action = document.getElementById('to').value.trim();
    const conditionType1 = document.getElementById('condition1').value.toUpperCase();
    const conditionText1 = document.getElementById('conditionText1').value.trim();
    const conditionType2 = document.getElementById('condition2').value.toUpperCase();
    const conditionText2 = document.getElementById('conditionText2').value.trim();
    // Capture the section and selected text
    const sectionId = extractSectionFromIframe(); // Get the section ID from the iframe
    const selectedText = lastSelectedText || '';
    
    console.log('Using selected text:', selectedText);
    // Create an annotation object with context
    const annotation = {
        annotation_type: 'text_annotation', // or another value based on your logic
        annotation_to_text: selectedText,
        annotation_to_text_section: sectionId,
        annotation_for_text: selectedText, // this can be modified depending on your logic
        annotation_for_section: sectionId, // For the section being annotated
        condition1_type: conditionType1,
        condition1_text: conditionText1,
        condition1_section: sectionId, // reference to section for condition 1
        condition2_type: conditionType2,
        condition2_text: conditionText2,
        condition2_section: sectionId // reference to section for condition 2
    };

    // Add the annotation to the array
    annotations.push(annotation);

    // Update the display in the annotations area
    document.getElementById('annotations-area').value += `------------------------\nIT IS ${deontic}\nFOR ${forEntity}\nTO ${action}\n${conditionType1} ${conditionText1}\n${conditionType2} ${conditionText2}\n`;

    // Clear form fields
    clearFormFields();

    // Print annotations to console for debugging
    console.log('Current annotations:', annotations);
}

/*
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
*/
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
    a.download = 'annotations.txt';
    document.body.appendChild(a);
    a.click();
    window.URL.revokeObjectURL(url);
    a.remove();
}
