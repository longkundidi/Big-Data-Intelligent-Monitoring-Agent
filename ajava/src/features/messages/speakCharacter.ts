import { wait } from "@/utils/wait";
import { synthesizeVoiceApi } from "./synthesizeVoice";
import { Viewer } from "../vrmViewer/viewer";
import { Screenplay } from "./messages";
import { Talk } from "./messages";
import { audio, audio_2 } from "./audio"

const createSpeakCharacter = () => {
  let lastTime = 0;
  let prevFetchPromise: Promise<unknown> = Promise.resolve();
  let prevSpeakPromise: Promise<unknown> = Promise.resolve();

  return (
      screenplay: Screenplay,
      viewer: Viewer,
      koeiroApiKey: string,
      onStart?: () => void,
      onComplete?: () => void
  ) => {
    const fetchPromise = prevFetchPromise.then(async () => {
      const now = Date.now();
      if (now - lastTime < 1000) {
        await wait(1000 - (now - lastTime));
      }

      const buffer = await fetchAudio(screenplay.talk, koeiroApiKey).catch(
          () => null
      );
      lastTime = Date.now();
      return buffer;
    });

    prevFetchPromise = fetchPromise;
    prevSpeakPromise = Promise.all([fetchPromise, prevSpeakPromise]).then(
        ([audioBuffer]) => {
          onStart?.();
          if (!audioBuffer) {
            return;
          }
          return viewer.model?.speak(audioBuffer, screenplay);
        }
    );
    prevSpeakPromise.then(() => {
      onComplete?.();
    });
  };
};

export const speakCharacter = createSpeakCharacter();

export const fetchAudio = async (
    talk: Talk,
    apiKey: string
): Promise<ArrayBuffer> => {
  // const ttsVoice = await synthesizeVoiceApi(
  //   talk.message,
  //   talk.speakerX,
  //   talk.speakerY,
  //   talk.style,
  //   apiKey
  // );
  // let url = "";
  // if (talk.message == "1") {
  //   url = audio;
  //   console.log("1")
  // }
  // else { url = audio_2; console.log("2")}
  // console.log("1111",url)
  const url = audio_2;
  if (url == null) {
    throw new Error("Something went wrong");
  }

  const resAudio = await fetch(url);
  console.log("resAudio", resAudio)
  const buffer = await resAudio.arrayBuffer();
  return buffer;
};
