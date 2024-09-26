document.addEventListener("DOMContentLoaded", function (event) {
    loadMap();
});
function loadMap() {
    var address = document.getElementById("address").value;
    var geocoder = new google.maps.Geocoder();

    geocoder.geocode({ address: address }, function
        (results, status) {
        if (status === "OK") {
            var mapOptions = {
                center: results[0].geometry.location,

                zoom: 15
            };

            var map = new google.maps.Map(document.getElementById("map"), mapOptions);

            var marker = new google.maps.Marker({
                position: results[0].geometry.location,
                map: map

            });
        } else {
            console.log("Geocoding failed: " + status);
        }
    });
}