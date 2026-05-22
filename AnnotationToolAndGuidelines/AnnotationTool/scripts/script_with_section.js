const sections = [
    "section-1",
    "section-2",
    "section-3",
    "section-4",
    "section-5",
    "section-6",
    "section-7",
    "section-8",
    "section-9",
    "section-10",
    "section-11",
    "section-12",
    "section-13",
    "section-14",
    "section-15",
    "section-16",
    "section-17",
    "section-18",
    "section-19",
    "section-19A",
    "section-20",
    "section-21",
    "section-22",
    "section-23",
    "section-24",
    "section-25",
    "section-26",
    "section-27",
    "section-28",
    "section-29",
    "section-30",
    "section-31",
    "section-32",
    "section-33",
    "section-34",
    "section-35",
    "section-36",
    "section-37",
    "section-38",
    "section-39",
    "section-40",
    "section-40A",
    "section-41",
    "section-42",
    "section-43",
    "section-44",
    "section-45",
    "section-46",
    "section-47",
    "section-48",
    "section-49",
    "section-50",
    "section-51",
    "section-52",
    "section-53",
    "section-54",
    "section-55",
    "section-56",
    "section-57",
    "section-58",
    "section-59",
    "section-60",
    "section-60A",
    "section-61",
    "section-62",
    "section-63",
    "section-64",
    "section-65",
    "section-66",
    "section-67",
    "section-68",
    "section-69",
    "section-70",
    "section-71",
    "section-72",
    "section-73",
    "section-74",
    "section-75",
    "section-76",
    "section-77",
    "section-78",
    "section-79",
    "section-80",
    "section-81",
    "section-82",
    "section-83",
    "section-84",
    "section-85",
    "section-86",
    "section-87",
    "section-88",
    "section-89",
    "section-90",
    "section-91",
    "section-92",
    "section-93",
    "section-94",
    "section-95",
    "section-96",
    "section-97",
    "section-98",
    "section-99",
    "section-100",
    "section-101",
    "section-102",
    "section-103",
    "section-104",
    "section-105",
    "section-106",
    "section-107",
    "section-108",
    "section-109",
    "section-110",
    "section-111",
    "section-112",
    "section-113",
    "section-114",
    "section-115",
    "section-116",
    "section-117",
    "section-118",
    "section-119",
    "section-120",
    "section-121",
    "section-122",
    "section-123",
    "section-124",
    "section-124A",
    "section-125",
    "section-126",
    "section-127",
    "section-128",
    "section-129",
    "section-130",
    "section-131",
    "section-132",
    "section-133",
    "section-134",
    "section-135",
    "section-136",
    "section-137",
    "section-138",
    "section-139",
    "section-139A",
    "section-140",
    "section-140A",
    "section-140AA",
    "section-140B",
    "section-141",
    "section-142",
    "section-143",
    "section-144",
    "section-145",
    "section-146",
    "section-147",
    "section-148",
    "section-149",
    "section-150",
    "section-151",
    "section-152",
    "section-153",
    "section-154",
    "section-155",
    "section-156",
    "section-157",
    "section-158",
    "section-159",
    "section-160",
    "section-161",
    "section-162",
    "section-163",
    "section-164",
    "section-164A",
    "section-165",
    "section-165A",
    "section-166",
    "section-167",
    "section-167A",
    "section-168",
    "section-169",
    "section-170",
    "section-171",
    "section-172",
    "section-173",
    "section-174",
    "section-175",
    "section-176",
    "section-177",
    "section-178",
    "section-179",
    "section-180",
    "section-181",
    "section-181A",
    "section-181B",
    "section-181C",
    "section-181D",
    "section-182",
    "section-183",
    "section-184",
    "section-185",
    "section-186",
    "section-187",
    "section-188",
    "section-189",
    "section-190",
    "section-191",
    "section-192",
    "section-193",
    "section-194",
    "section-195",
    "section-196",
    "section-197",
    "section-198",
    "section-199",
    "section-200",
    "section-202",
    "section-203",
    "section-204",
    "section-205",
    "section-206",
    "section-207",
    "section-208",
    "section-209",
    "section-210",
    "section-211",
    "section-212",
    "section-213",
    "section-214",
    "section-215",
    "section-216",
    "section-217",
    "section-218",
    "schedule-1-part-1",
    "schedule-1-part-2",
    "schedule-3-part-1",
    "schedule-3-part-2",
    "schedule-3-part-3",
    "schedule-3-part-4",
    "schedule-3-part-5",
    "schedule-3-part-6",
    "schedule-3-part-6ZA",
    "schedule-3-part-6A",
    "schedule-3-part-6B",
    "schedule-3-part-7",
    "schedule-3-part-8",
    "schedule-3-part-9",
    "schedule-3-part-10",
    "schedule-7-part-1",
    "schedule-7-part-2",
    "schedule-8-part-1",
    "schedule-8-part-2",
    "schedule-8-part-3",
    "schedule-9-part-1",
    "schedule-9-part-2",
    "schedule-9-part-3",
    "schedule-11-part-1",
    "schedule-11-part-2",
    "schedule-11-part-3",
    "schedule-12-part-1",
    "schedule-12-part-2",
    "schedule-17-part-1",
    "schedule-17-part-2",
    "schedule-17-part-3",
    "schedule-17-part-4",
    "schedule-19-part-1",
    "schedule-19-part-2",
    "schedule-19-part-3",
    "schedule-19-part-4",
    "schedule-26-part-2",
    "schedule-27-part-1",
    "schedule-27-part-1A",
    "schedule-27-part-2",
    "schedule-27-part-3"
];
// Function to populate section dropdowns
function populateSectionDropdowns() {
    console.log('Starting to populate sections...');

    const dropdowns = [
        'deonticSection',
        'condition1Section',
        'condition2Section'
    ];
    
    dropdowns.forEach(dropdownId => {
        const dropdown = document.getElementById(dropdownId);
        if (!dropdown) {
            console.error(`Dropdown element not found: ${dropdownId}`);
            return;
        }
        
        // Clear existing options except the first one
        while (dropdown.options.length > 1) {
            dropdown.remove(1);
        }
        
        // Add sections directly from the sections array
        sections.forEach(section => {
            const option = document.createElement('option');
            option.value = section;
            option.textContent = section;
            dropdown.appendChild(option);
        });
    });
}

// Call populateSectionDropdowns when the page loads
document.addEventListener('DOMContentLoaded', function() {
    console.log('DOM Content Loaded');
    populateSectionDropdowns();
});

// Function to add annotations from the form into the array and clear the inputs
function addAnnotation() {
    // Retrieve values from the form
    const deontic = document.getElementById('deontic').value.toUpperCase();
    const deonticSection = document.getElementById('deonticSection').value;
    const forEntity = document.getElementById('for').value.trim();
    const action = document.getElementById('to').value.trim();
    const conditionType1 = document.getElementById('condition1').value.toUpperCase();
    const conditionText1 = document.getElementById('conditionText1').value.trim();
    const condition1Section = document.getElementById('condition1Section').value;
    const conditionType2 = document.getElementById('condition2').value.toUpperCase();
    const conditionText2 = document.getElementById('conditionText2').value.trim();
    const condition2Section = document.getElementById('condition2Section').value;

    // Create sections array - only include sections that have corresponding annotations
    let sections = [];
    
    // Always include main section if deontic is selected
    if (deontic && deonticSection) {
        sections.push(`main_${deonticSection}`);
    }
    
    // Include condition sections only if they have text
    if (conditionText1 && condition1Section) {
        sections.push(`condition1_${condition1Section}`);
    }
    if (conditionText2 && condition2Section) {
        sections.push(`condition2_${condition2Section}`);
    }

    // Update the display in the annotations area
    let annotationText = '';
    
    // Add section list with clear separation
    if (sections.length > 0) {
        annotationText += `=========\n`;
        annotationText += `[${sections.join(',')}]\n`;
        annotationText += `=========\n\n`;
    }
    
    // Add main annotation
    annotationText += `IT IS ${deontic}\n`;
    if (forEntity) {
        annotationText += `FOR ${forEntity}\n`;
    }
    if (action) {
        annotationText += `TO ${action}\n`;
    }
    
    // Add conditions if they exist
    if (conditionText1) {
        annotationText += `${conditionType1} ${conditionText1}\n`;
    }
    if (conditionText2) {
        annotationText += `${conditionType2} ${conditionText2}\n`;
    }
    
    // Add separator
    annotationText += `------------------------\n`;

    // Append to annotations area
    document.getElementById('annotations-area').value += annotationText;

    // Clear form fields but preserve section selections
    clearFormFields();
}

// Function to clear form fields after adding an annotation
function clearFormFields() {
    // Clear only the annotation fields, preserve section selections
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
