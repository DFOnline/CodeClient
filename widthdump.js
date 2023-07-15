/**
 * @author GeorgeRNG
 * You need nodejs, and recode on your game
 * Use /widthdump
 * Put the output file in the same folder at this
 * Run `node widthdump.js` with a terminal in the folder
 * An old tool of mine can use recode to import this: https://georgerng.github.io/GeorgeRNG.github.io-old/dfcode/index.html
 * Copy the data from output.txt into the area which says it has the raw json
 * If you have Recode Item API enabled there should be a `Send to CodeUtilities` button, which will import it (if your player is in creative)


 * Yeah I'm never really elegant at importing data like this.

*/

const fs = require('fs/promises');

(async () => {
    const width = await fs.readFile('./widthdump.txt');
    const data = '{"blocks":[{"id":"block","block":"func","args":{"items":[{"item":{"id":"bl_tag","data":{"option":"False","tag":"Is Hidden","action":"dynamic","block":"func"}},"slot":26}]},"data":"widthdump"},{"id":"block","block":"set_var","args":{"items":[{"item":{"id":"var","data":{"name":"width-0","scope":"local"}},"slot":0},{"item":{"id":"txt","data":{"name":"WITH0"}},"slot":1},{"item":{"id":"txt","data":{"name":""}},"slot":2}]},"action":"SplitText"},{"id":"block","block":"set_var","args":{"items":[{"item":{"id":"var","data":{"name":"width-1","scope":"local"}},"slot":0},{"item":{"id":"txt","data":{"name":"WITH1"}},"slot":1},{"item":{"id":"txt","data":{"name":""}},"slot":2}]},"action":"SplitText"},{"id":"block","block":"set_var","args":{"items":[{"item":{"id":"var","data":{"name":"width-2","scope":"local"}},"slot":0},{"item":{"id":"txt","data":{"name":"WITH2"}},"slot":1},{"item":{"id":"txt","data":{"name":""}},"slot":2}]},"action":"SplitText"},{"id":"block","block":"set_var","args":{"items":[{"item":{"id":"var","data":{"name":"width-3","scope":"local"}},"slot":0},{"item":{"id":"txt","data":{"name":"WITH3"}},"slot":1},{"item":{"id":"txt","data":{"name":""}},"slot":2}]},"action":"SplitText"},{"id":"block","block":"set_var","args":{"items":[{"item":{"id":"var","data":{"name":"width-4","scope":"local"}},"slot":0},{"item":{"id":"txt","data":{"name":"WITH4"}},"slot":1},{"item":{"id":"txt","data":{"name":""}},"slot":2}]},"action":"SplitText"},{"id":"block","block":"set_var","args":{"items":[{"item":{"id":"var","data":{"name":"width-5","scope":"local"}},"slot":0},{"item":{"id":"txt","data":{"name":"WITH5"}},"slot":1},{"item":{"id":"txt","data":{"name":""}},"slot":2}]},"action":"SplitText"},{"id":"block","block":"set_var","args":{"items":[{"item":{"id":"var","data":{"name":"width-6","scope":"local"}},"slot":0},{"item":{"id":"txt","data":{"name":"WITH6"}},"slot":1},{"item":{"id":"txt","data":{"name":""}},"slot":2}]},"action":"SplitText"},{"id":"block","block":"set_var","args":{"items":[{"item":{"id":"var","data":{"name":"width-7","scope":"local"}},"slot":0},{"item":{"id":"txt","data":{"name":"WITH7"}},"slot":1},{"item":{"id":"txt","data":{"name":""}},"slot":2}]},"action":"SplitText"},{"id":"block","block":"set_var","args":{"items":[{"item":{"id":"var","data":{"name":"width-8","scope":"local"}},"slot":0},{"item":{"id":"txt","data":{"name":"WITH8"}},"slot":1},{"item":{"id":"txt","data":{"name":""}},"slot":2}]},"action":"SplitText"},{"id":"block","block":"set_var","args":{"items":[{"item":{"id":"var","data":{"name":"width-9","scope":"local"}},"slot":0},{"item":{"id":"txt","data":{"name":"WITH9"}},"slot":1},{"item":{"id":"txt","data":{"name":""}},"slot":2}]},"action":"SplitText"},{"id":"block","block":"set_var","args":{"items":[{"item":{"id":"var","data":{"name":"width-10","scope":"local"}},"slot":0},{"item":{"id":"txt","data":{"name":"WITH10"}},"slot":1},{"item":{"id":"txt","data":{"name":""}},"slot":2}]},"action":"SplitText"}]}';
    const json = JSON.parse(data)
    const widths = width.toString().split('\n');
    [0,1,2,3,4,5,6,7,8,9,10].forEach(w => {
        json.blocks[w + 1].args.items[1].item.data.name = widths.filter(char => char.endsWith(` ${w}`)).map(char => char.substr(0,1)).join('').substr(0,4000);
    })
    const output = JSON.stringify(json);
    fs.writeFile('./output.txt',output)
})()