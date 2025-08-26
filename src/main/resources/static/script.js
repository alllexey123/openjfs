let path = window.location.pathname.substring(4);
updateData(path);

function updateData(path) {
    fetch(`/list/${path}`)
        .then((response) => response.json())
        .then((data) => {
            updateContent(data)
            updateTopbar(data)
        });
}

function updateWindowPath(path) {
    const newUrl = `/ui/${path}`;
    window.history.pushState({path: newUrl}, '', newUrl);
}

window.addEventListener('popstate', (event) => {
    if (event.state && event.state.path) {
        const newPath = event.state.path.substring(4);
        updateData(newPath);
    } else {
        const initialPath = window.location.pathname.substring(4);
        updateData(initialPath);
    }
});

function updateContent(data) {
    const fileContentWrapper = document.querySelector('.file-content-wrapper');
    if (data['type'] === 'DIRECTORY') {
        let files = data['files']

        files = files == null ? [] : files;
        updateFileList(files);
        updateFileContent('');

        fileContentWrapper.classList.add('hidden');
    } else {
        let fileContentTitle = document.querySelector('.file-content-title');
        let fileContent = document.querySelector('.file-content');
        if (data['size'] <= 128 * 1024) { // max 128KB
            fetch(`/text/${data['path'] + data['name']}`)
                .then(value => value.text())
                .then(text => {
                    fileContent.classList.remove('hidden');
                    fileContentTitle.textContent = 'File contents:';
                    updateFileContent(text)
                });
        } else {
            fileContentTitle.textContent = 'File is too huge to display.'
            fileContent.classList.add('hidden');
        }

        updateFileList([data]);

        fileContentWrapper.classList.remove('hidden');
    }
}

function updateFileList(files) {
    const fileList = document.querySelector('.file-list');
    fileList.innerHTML = '';

    sortFileList(files);
    for (let fileKey in files) {
        let file = files[fileKey];
        let listItem = createListItem(file);
        fileList.appendChild(listItem);
    }
}

function sortFileList(files) {
    files.sort((a, b) => {
        if (a.type === "DIRECTORY" && b.type !== "DIRECTORY") return -1;
        if (a.type !== "DIRECTORY" && b.type === "DIRECTORY") return 1;

        return a.name.toLowerCase().localeCompare(b.name.toLowerCase());
    });
}

function updateFileContent(text) {
    let fileContent = document.querySelector('.file-content');
    fileContent.value = text;
}

function updateTopbar(data) {
    updateTopbarPath(data);
    updateTopbarButtons(data);
}

function updateTopbarPath(data) {
    let topbarPathDiv = document.querySelector('.topbar-path');
    let serverNameSegment = topbarPathDiv.querySelector('.server-name').cloneNode(true);
    addLinkListener(serverNameSegment);
    topbarPathDiv.innerHTML = '';
    topbarPathDiv.appendChild(serverNameSegment);
    let fullPath = data['path'] + data['name'];

    let splitPath = fullPath.split('/');
    let pathBuffer = '';

    if (data['type'] !== 'DIRECTORY') {
        splitPath = splitPath.slice(0, splitPath.length - 1);
    }

    for (let pathSegmentStr of splitPath) {
        if (pathSegmentStr === '') continue;
        pathBuffer += pathSegmentStr + '/';

        let arrow = document.createElement('img');
        arrow.classList.add('topbar-arrow')
        arrow.src = '/svg/arrow.svg';
        arrow.alt = 'arrow';

        let a = document.createElement('a');
        a.href = '/ui/' + pathBuffer;
        a.text = pathSegmentStr;
        addLinkListener(a);

        topbarPathDiv.appendChild(arrow);
        topbarPathDiv.appendChild(a);
    }
}

function updateTopbarButtons(data) {
    let topbarDownload = document.querySelector('.topbar-download');
    let isDirectory = data['type'] === 'DIRECTORY';
    if (isDirectory && !allowDownloadDirs) topbarDownload.classList.add('unavailable');
    else topbarDownload.classList.remove('unavailable');
}

function addLinkListener(a) {
    a.addEventListener('click', (event) => {
        event.preventDefault();
        const newPath = a.getAttribute('href').substring(4);
        updateData(newPath);
        updateWindowPath(newPath);
    });
}

function createListItem(file) {
    let fileType = file['type'];
    let isDirectory = fileType === 'DIRECTORY';
    let fileName = file['name'];
    let fileLastModifiedRaw = file['lastModified'];
    let path = file['path'];
    let fileLastModified = fileLastModifiedRaw === null ? '—' : formatDate(fileLastModifiedRaw);
    let fileSize = !isDirectory ? formatBytes(file['size']) : '—';

    const listItem = document.createElement('div');
    listItem.classList.add('file-row');
    if (isDirectory) {
        listItem.classList.add('dir-row')
    }
    const colName = document.createElement('div');

    colName.classList.add('col', 'name');
    const icon = document.createElement('img');
    icon.classList.add('icon');
    icon.alt = isDirectory ? 'dir' : 'file';
    icon.src = '/svg/' + getSvgName(fileName, isDirectory);
    const nameTextNode = document.createTextNode(fileName);
    colName.appendChild(icon);
    colName.appendChild(nameTextNode);
    const colModified = document.createElement('div');

    colModified.classList.add('col', 'modified');
    const modifiedTextNode = document.createTextNode(fileLastModified);
    colModified.appendChild(modifiedTextNode);
    const colSize = document.createElement('div');

    colSize.classList.add('col', 'size');
    const sizeTextNode = document.createTextNode(fileSize);
    colSize.appendChild(sizeTextNode);
    const colActions = document.createElement('div');

    colActions.classList.add('col', 'actions');
    const downloadButton = document.createElement('button');
    const downloadIcon = document.createElement('img');
    if (isDirectory && !allowDownloadDirs) downloadButton.classList.add('unavailable')
    downloadIcon.classList.add('icon');
    downloadIcon.alt = 'download';
    downloadIcon.src = '/svg/download.svg';
    downloadButton.appendChild(downloadIcon);
    colActions.appendChild(downloadButton);
    const linkButton = document.createElement('button');
    const linkIcon = document.createElement('img');
    linkIcon.classList.add('icon');
    linkIcon.alt = 'link';
    linkIcon.src = '/svg/link.svg';
    linkButton.appendChild(linkIcon);
    colActions.appendChild(linkButton);
    listItem.onclick = () => {

        // if (!isDirectory) return;
        updateData(path + fileName);
        updateWindowPath(path + fileName);
    }
    linkButton.onclick = (e) => {

        e.stopPropagation();
        navigator.clipboard.writeText(window.location.origin + '/' + path + fileName)
    }
    downloadButton.onclick = (e) => {

        e.stopPropagation();
        if (isDirectory && !allowDownloadDirs) return;
        window.location.pathname = '/direct/' + path + fileName;
    }

    if (fileName.startsWith('.')) {
        colName.classList.add('hidden-file');
    }

    listItem.appendChild(colName);
    listItem.appendChild(colModified);
    listItem.appendChild(colSize);
    listItem.appendChild(colActions);

    return listItem;
}

const getSvgName = (filename, isDirectory) => {
    if (isDirectory) {
        return 'directory.svg';
    }

    const ext = getFileExtension(filename);

    switch (ext) {
        // Spreadsheets
        case 'xls':
        case 'xlsx':
        case 'ods':
        case 'csv':
            return 'file-chart.svg';

        // Documents
        case 'txt':
        case 'doc':
        case 'docx':
        case 'pdf':
        case 'odt':
        case 'rtf':
            return 'file-pencil.svg';

        // Code
        case 'py':
        case 'java':
        case 'js':
        case 'html':
        case 'css':
        case 'cpp':
        case 'c':
        case 'json':
        case 'xml':
        case 'sh':
        case 'bat':
            return 'file-code.svg';

        // Images
        case 'png':
        case 'jpg':
        case 'jpeg':
        case 'gif':
        case 'svg':
        case 'bmp':
        case 'ico':
        case 'tif':
        case 'tiff':
        case 'webp':
            return 'file-alt.svg';

        // Audio
        case 'mp3':
        case 'wav':
        case 'ogg':
        case 'aac':
        case 'wma':
        case 'aif':
        case 'flac':
        case 'midi':
            return 'file-audio.svg';

        // Video
        case 'mp4':
        case 'avi':
        case 'mov':
        case 'wmv':
        case 'mkv':
        case 'flv':
            return 'file-audio.svg';

        // Compressed files
        case 'zip':
        case 'rar':
        case '7z':
        case 'tar':
        case 'gz':
            return 'file-lock.svg';

        // Executable files
        case 'exe':
        case 'msi':
        case 'bin':
            return 'file-exclamation.svg';

        case '':
            return 'file-question.svg';
        // Default case
        default:
            return 'file-alt.svg';
    }
};

const getFileExtension = (filename) => {
    if (typeof filename !== 'string') {
        return '';
    }
    const lastDotIndex = filename.lastIndexOf('.');
    if (lastDotIndex === -1 || lastDotIndex === 0) {
        return '';
    }
    return filename.substring(lastDotIndex + 1).toLowerCase();
};

const formatDate = (dateString) => {
    const date = new Date(dateString);
    const months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
    const month = months[date.getMonth()];
    const day = date.getDate();
    const year = date.getFullYear();
    return `${month} ${day}, ${year}`;
};

const formatBytes = (bytes, decimals = 1) => {
    if (bytes === 0) return '0 Bytes';

    const k = 1024;
    const dm = decimals < 0 ? 0 : decimals;
    const sizes = ['B', 'KB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB'];

    const i = Math.floor(Math.log(bytes) / Math.log(k));

    let formattedValue = Math.floor(bytes / Math.pow(k, i) * Math.pow(10, dm)) / Math.pow(10, dm);

    if (formattedValue % 1 === 0) {
        return formattedValue + ' ' + sizes[i];
    } else {
        return formattedValue.toFixed(dm) + ' ' + sizes[i];
    }
}

