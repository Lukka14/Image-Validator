async function showWebPageStatus(event) {
    event.preventDefault();

    const textarea = document.getElementById('urlTextArea');
    // Get the value of the textarea
    const urlsText = textarea.value;
    // Split the value by line breaks to obtain individual URLs
    const urls = urlsText.split('\n');
    // Remove leading and trailing white spaces from each URL
    const trimmedURLs = urls.map((url) => url.trim()).filter((url) => url !== "");

    const pageListElement = document.getElementById('pageList');

    let  pageListItemOriginal = pageListElement.getElementsByClassName('pageListItem')[0];

    let scanningText = 'Scanning web pages: ';
    let showInvalidPicturesText = 'Show invalid pictures ';
    let index = 1;

    let proxyEnabled = document.getElementById('proxyEnabled').checked;
    let loader = document.getElementById('loader');
    let isAlerted = false;

    for (let url of trimmedURLs) {
        document.getElementById('scanning').innerHTML = scanningText + index + '/' + trimmedURLs.length;

        let pageListItemCopy = pageListItemOriginal.cloneNode(true);
        pageListItemCopy.style.display = 'block';
        pageListElement.appendChild(pageListItemCopy);

        let invalidPicsBtn = pageListItemCopy.getElementsByClassName('invalidPicturesBtn')[0];
        const container = pageListItemCopy.getElementsByClassName('status')[0];

        let pageUrlElement = pageListItemCopy.getElementsByClassName('pageUrl')[0];
        pageUrlElement.innerHTML = url;
        pageUrlElement.setAttribute('href', url);

        index++;
        const requestUrl = 'api/getImageSrcFromPage?webPageUrl=' + url;

        loader.style.display = 'block';

        const fetchImageSrc = fetch(requestUrl)
            .then(response => response.json());

        const imageSrcData = await fetchImageSrc;

        const imageUrlSet = new Set(imageSrcData);

        const quantity = imageUrlSet.size;

        // Get the container element where the list will be appended

        let validPicCount = 0;

        // setPageStatus(pageUrlElement, url);
        if (quantity <= 1) {
            pageUrlElement.style.color = 'RED';
            loader.style.display = 'none';
            invalidPicsBtn.style.display = 'none';
            continue;
        }
        pageUrlElement.style.color = 'GREEN';

        const fetchImageRequests = Array.from(imageUrlSet).map(imageUrl => {
            const imageRequestUrl = 'api/getImageRequestStatus?webPageUrl=' + imageUrl + '&proxyEnabled=' + proxyEnabled;
            return fetch(imageRequestUrl)
                .then(response => response.json());
        });

        const imageRequestStatuses = await Promise.all(fetchImageRequests);

        for (let i = 0; i < imageRequestStatuses.length; i++) {
            const imageStatus = imageRequestStatuses[i];
            const imageUrl = Array.from(imageUrlSet)[i];

            invalidPicsBtn.innerHTML = showInvalidPicturesText + (quantity - validPicCount) + '/' + quantity;

            if (imageStatus !== 200) {
                if(imageStatus === 403){
                    pageListItemCopy.getElementsByClassName('permissionWarning')[0].style.display = 'inline-block';

                    if (!isAlerted) {
                        isAlerted = true;
                    }
                }

                const listElement = document.createElement('li');
                listElement.innerHTML = '<a href="' + imageUrl + '" target="_blank"> ' + imageUrl + ' </a> &nbsp - &nbsp ' + imageStatus;

                container.appendChild(listElement);
            } else {
                validPicCount++;
                invalidPicsBtn.innerHTML = showInvalidPicturesText + (quantity - validPicCount) + '/' + quantity;
            }
        }
        if(validPicCount===quantity){
            invalidPicsBtn.style.display = 'none';
        }

    }

    loader.style.display = 'none';
    if (isAlerted) {
        window.alert('Looks like you have got permission denied on some pictures, please re-try with enabled proxy.');
    }
    // Call setLoader() after both fetch functions are finished
    // event.target.reset();
}

function enablePlayList(){
    document.getElementById('pageList').style.display = 'block';
}

function showInvalidPictures(element) {
    let imageUrlContainer = element.getElementsByClassName('status')[0];
    let btn = element.getElementsByClassName('invalidPicturesBtn')[0];
    if(btn.innerText.includes('0')){
        window.alert('No invalid pictures found.');
        return;
    }

    let display = imageUrlContainer.style.display;
    if(display==='none' || !display){
        btn.innerText = btn.innerText.replace('Show','Hide');
        imageUrlContainer.style.display = 'block';
    }else{
        btn.innerText = btn.innerText .replace('Hide','Show');
        imageUrlContainer.style.display = 'none';
    }
}
//
// async function showWebPageStatus(event) {
//     event.preventDefault();
//
//     const textarea = document.getElementById('urlTextArea');
//     // Get the value of the textarea
//     const urlsText = textarea.value;
//     // Split the value by line breaks to obtain individual URLs
//     const urls = urlsText.split('\n');
//     // Remove leading and trailing white spaces from each URL
//     const trimmedURLs = urls.map((url) => url.trim());
//
//     const pageListElement = document.getElementById('pageList');
//
//     let pageListItem = pageListElement.getElementsByClassName('pageListItem')[0];
//
//     let scanningText = 'Scanning web pages: ';
//     let showInvalidPicturesText = 'Show invalid pictures ';
//     let index = 1;
//
//     let proxyEnabled = document.getElementById('proxyEnabled').checked;
//
//     for (let url of trimmedURLs) {
//         if(index-1>0){
//             pageListElement.append(pageListItem.cloneNode(true));
//             pageListItem = pageListElement.getElementsByClassName('pageListItem')[index-1];
//         }
//         let invalidPics = pageListItem.getElementsByClassName('invalidPicturesBtn')[0];
//         document.getElementById('scanning').innerHTML = scanningText + index + '/' + trimmedURLs.length;
//         const container = pageListItem.getElementsByClassName('status')[0];
//
//         let pageUrlElement = pageListItem.getElementsByClassName('pageUrl')[0];
//         pageUrlElement.innerHTML = url;
//         pageUrlElement.setAttribute('href', url);
//
//         index++;
//         const requestUrl = 'api/getImageSrcFromPage?webPageUrl=' + url;
//
//         let loader = document.getElementById('loader');
//         loader.style.display = 'block';
//
//         fetch(requestUrl)
//             .then(response => response.json())
//             .then(data => {
//                 const imageUrlSet = new Set(data);
//                 let isAlerted = false;
//
//                 const quantity = imageUrlSet.size;
//
//                 // Get the container element where the list will be appended
//
//                 let validPicCount = 0;
//
//                 // setPageStatus(pageUrlElement, url);
//                 if (quantity <= 1) {
//                     pageUrlElement.style.color = 'RED';
//                     loader.style.display = 'none';
//                     return;
//                 }
//                 pageUrlElement.style.color = 'GREEN';
//                 for (let imageUrl of imageUrlSet) {
//                     const imageRequestUrl = 'api/getImageRequestStatus?webPageUrl=' + imageUrl + '&proxyEnabled=' + proxyEnabled;
//                     fetch(imageRequestUrl).then(response => response.json()).then(imageStatus => {
//
//                         invalidPics.innerHTML = showInvalidPicturesText + (quantity - validPicCount) + '/' + quantity
//                         if (imageStatus !== 200) {
//                             if (imageStatus === 403 && !isAlerted) {
//                                 isAlerted = true;
//                             }
//
//                             const listElement = document.createElement('li');
//                             listElement.innerHTML = '<a href="' + imageUrl + '" target="_blank"> ' + imageUrl + ' </a> &nbsp - &nbsp ' + imageStatus;
//
//                             container.appendChild(listElement);
//                         } else {
//                             validPicCount++;
//                             invalidPics.innerHTML = showInvalidPicturesText + (quantity - validPicCount) + '/' + quantity
//                         }
//                     });
//                 }
//                 // const listItem = document.createElement('br');
//                 // container.appendChild(listItem);
//
//                 if (isAlerted) {
//                     window.alert('Looks like permission is denied on some pictures, please re-try with proxy enabled.')
//                 }
//
//             })
//             .catch(error => {
//                 console.error('Error:', error);
//             });
//
//     }
//     // event.target.reset();
// }